package xyz.victorolaitan.scholar.controller;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.Util;

public class ScheduleEditCtrl implements FragmentCtrl {
    private Schedule schedule;
    private boolean updating, initIterative, initRelative;

    private RadioButton radioNoRepeat;
    private RadioButton radioIterative;
    private RadioButton radioRelative;
    private RadioButton radioEndOnDate;
    private RadioButton radioEndAfterTimes;
    private TextView txtStart;
    private TextView txtEndDate;
    private EditText editRepeatDelay;
    private EditText editEndDelay;
    private Spinner spinnerIterative;
    private Spinner spinnerRelative;

    private void checkRepeatNotNull() {
        if (schedule.getRepeatSchedule() == null) {
            schedule.initRepeatSchedule();
        }
    }

    @Override
    public void init(View view) {
        updating = true;
        spinnerIterative = view.findViewById(R.id.editSchedule_iterativeSpinner);
        spinnerRelative = view.findViewById(R.id.editSchedule_relativeSpinner);

        radioNoRepeat = view.findViewById(R.id.editSchedule_radioNoRepeat);
        radioNoRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!updating) {
                    schedule.setEndType(Schedule.EndType.ON_DATE);
                    schedule.setEnd(schedule.getStart());
                }
                radioIterative.setChecked(false);
                radioRelative.setChecked(false);
            }
        });
        radioIterative = view.findViewById(R.id.editSchedule_radioIterative);
        radioIterative.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!updating) {
                    checkRepeatNotNull();
                    schedule.getRepeatSchedule().setType(Schedule.RepeatType.ITERATIVE);
                }
                radioNoRepeat.setChecked(false);
                radioRelative.setChecked(false);
            }
        });
        radioRelative = view.findViewById(R.id.editSchedule_radioRelative);
        radioRelative.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!updating) {
                    checkRepeatNotNull();
                    if (schedule.getRepeatSchedule().getRepeatBasis() == Schedule.RepeatBasis.DAILY) {
                        schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
                    }
                    schedule.getRepeatSchedule().setWeeks(1);
                    schedule.getRepeatSchedule().setType(Schedule.RepeatType.RELATIVE);
                }
                radioNoRepeat.setChecked(false);
                radioIterative.setChecked(false);
            }
        });
        radioEndOnDate = view.findViewById(R.id.editSchedule_radioEndOnDay);
        radioEndOnDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!updating)
                    schedule.setEndType(Schedule.EndType.ON_DATE);
                radioEndAfterTimes.setChecked(false);
            }
        });
        radioEndAfterTimes = view.findViewById(R.id.editSchedule_radioEndAfterTimes);
        radioEndAfterTimes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!updating)
                    schedule.setEndType(Schedule.EndType.AFTER_TIMES);
                radioEndOnDate.setChecked(false);
            }
        });

        txtStart = view.findViewById(R.id.editSchedule_txtStart);
        txtStart.setOnClickListener(v -> changeStart(view));
        txtEndDate = view.findViewById(R.id.editSchedule_txtEndDate);
        txtEndDate.setOnClickListener(v ->changeEnd(view));
        view.findViewById(R.id.editSchedule_changeStart).setOnClickListener(v -> changeStart(view));
        view.findViewById(R.id.editSchedule_changeEndDate).setOnClickListener(v -> changeEnd(view));

        editRepeatDelay = view.findViewById(R.id.editSchedule_editRepeatDelay);
        if (schedule.getRepeatSchedule() != null) {
            editRepeatDelay.setText(String.valueOf(schedule.getRepeatSchedule().getDelay()));
        } else {
            editRepeatDelay.setText("1");
        }
        editRepeatDelay.addTextChangedListener((TextListener) s -> {
            if (s.length() > 0) {
                checkRepeatNotNull();
                schedule.getRepeatSchedule().setDelay(Integer.parseInt(editRepeatDelay.getText().toString()));
            }
        });

        editEndDelay = view.findViewById(R.id.editSchedule_editEndDelay);
        if (schedule.getEndType() == Schedule.EndType.ON_DATE) {
            editEndDelay.setText("1");
        } else {
            editEndDelay.setText(String.valueOf(schedule.getRepeatSchedule().getMaxOccurrences()));
        }
        editEndDelay.addTextChangedListener((TextListener) s -> {
            if (s.length() > 0) {
                schedule.getRepeatSchedule()
                        .endAfterXTimes(Integer.parseInt(editEndDelay.getText().toString()));
                radioEndAfterTimes.toggle();
            }
        });

        if (schedule.getEndType() == Schedule.EndType.ON_DATE)
            radioEndOnDate.toggle();
        else
            radioEndAfterTimes.toggle();

        schedule.addChangeListener(scheduleListener);
    }

    private void changeStart(View view) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(schedule.getStart());
        Util.newDatePicker(view, cal, schedule, Util.SCHEDULE_START, false).show();
    }

    private void changeEnd(View view) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(schedule.getEnd());
        DatePickerDialog datePicker = Util.newDatePicker(view, cal, schedule, Util.SCHEDULE_END, false);
        datePicker.getDatePicker().setMinDate(schedule.getStart().getTime());
        datePicker.show();
    }

    @Override
    public void updateInfo() {
        updating = true;
        txtStart.setText(Util.formatDate(schedule.getStart()));
        txtEndDate.setText(Util.formatDate(schedule.getEnd()));
        if (schedule.getRepeatSchedule() == null) {
            if (!radioNoRepeat.isChecked()) {
                radioNoRepeat.setChecked(true);
            }
        } else if (schedule.getRepeatSchedule().getType() == Schedule.RepeatType.ITERATIVE) {
            if (!radioIterative.isChecked()) {
                radioIterative.setChecked(true);
                updateIterativeSpinner();
            }
        } else if (!radioRelative.isChecked()) {
            radioRelative.setChecked(true);
            updateRelativeSpinner();
        }
        updating = false;
    }

    private void updateIterativeSpinner() {
        checkRepeatNotNull();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                spinnerIterative.getContext(), android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIterative.setAdapter(spinnerAdapter);
        for (int i = 1; i <= 4; i++) {
            int res;
            if (i == 1) {
                res = R.plurals.editSchedule_basisDay;
            } else if (i == 2) {
                res = R.plurals.editSchedule_basisWeek;
            } else if (i == 3) {
                res = R.plurals.editSchedule_basisMonth;
            } else {
                res = R.plurals.editSchedule_basisYear;
            }
            spinnerAdapter.add(spinnerIterative.getResources()
                    .getQuantityString(res,
                            schedule.getRepeatSchedule() == null ? 1 : schedule.getRepeatSchedule().getDelay()));
        }
        spinnerAdapter.notifyDataSetChanged();

        spinnerIterative.setSelection(schedule.getRepeatSchedule().getRepeatBasis().getIndex());

        initIterative = false;
        spinnerIterative.setOnItemSelectedListener((SpinnerListener) (parent, view, position, id) -> {
            if (!initIterative) {
                initIterative = true;
                return;
            }
            checkRepeatNotNull();
            schedule.getRepeatSchedule().setType(Schedule.RepeatType.ITERATIVE);
            if (position == 0)
                schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.DAILY);
            else if (position == 1)
                schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
            else if (position == 2)
                schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
            else
                schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.YEARLY);
        });
    }

    private void updateRelativeSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                spinnerRelative.getContext(), android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelative.setAdapter(spinnerAdapter);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(schedule.getStart());
        String[] firstSecondEtc = spinnerRelative.getResources().getStringArray(R.array.firstSecondThirdEtcArr);

        int dayOMonth = calendar.get(Calendar.DAY_OF_MONTH);
        spinnerAdapter.add(spinnerRelative.getResources().getString(R.string.editSchedule_repeatSpinnerItemFormat,
                dayOMonth,
                firstSecondEtc[dayOMonth <= 3 ? dayOMonth - 1 : 3],
                spinnerRelative.getResources().getQuantityString(R.plurals.editSchedule_basisDay, 1),
                spinnerRelative.getResources().getQuantityString(R.plurals.editSchedule_basisMonth, 1)));

        int weekOMonth = calendar.get(Calendar.WEEK_OF_MONTH);
        spinnerAdapter.add(spinnerRelative.getResources().getString(R.string.editSchedule_repeatSpinnerItemFormat,
                weekOMonth,
                firstSecondEtc[weekOMonth != 4 ? weekOMonth - 1 : 3],
                spinnerRelative.getResources().getQuantityString(R.plurals.editSchedule_basisWeek, 1),
                spinnerRelative.getResources().getQuantityString(R.plurals.editSchedule_basisMonth, 1)));

        spinnerAdapter.add(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
        spinnerAdapter.notifyDataSetChanged();

        if (schedule.getRepeatSchedule().hasDays()) {
            spinnerRelative.setSelection(0);
        } else if (schedule.getRepeatSchedule().hasWeeks()) {
            spinnerRelative.setSelection(1);
        } else if (schedule.getRepeatSchedule().hasMonths()) {
            spinnerRelative.setSelection(2);
        }

        initRelative = false;
        spinnerRelative.setOnItemSelectedListener((SpinnerListener) (parent, view, position, id) -> {
            if (!initRelative) {
                initRelative = true;
                return;
            }
            checkRepeatNotNull();
            schedule.getRepeatSchedule().clearRelatives();
            if (position == 0) {
                schedule.getRepeatSchedule().setDays(dayOMonth);
                schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
            } else if (position == 1) {
                schedule.getRepeatSchedule().setWeeks(weekOMonth);
                schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
            } else {
                schedule.getRepeatSchedule().setMonths(calendar.get(Calendar.MONTH) + 1);
                schedule.getRepeatSchedule().setRepeatBasis(Schedule.RepeatBasis.YEARLY);
            }
            schedule.getRepeatSchedule().setType(Schedule.RepeatType.RELATIVE);
        });
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    private interface TextListener extends android.text.TextWatcher {
        @Override
        default void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        default void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private interface SpinnerListener extends AdapterView.OnItemSelectedListener {
        @Override
        default void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private Schedule.ChangeListener scheduleListener = new Schedule.ChangeListener() {
        @Override
        public void onStartChange(Date newStart) {
            txtStart.setText(Util.formatDate(schedule.getStart()));
        }

        @Override
        public void onEndChange(Date newEnd) {
            txtEndDate.setText(Util.formatDate(schedule.getEnd()));
        }

        @Override
        public void onEndTypeChange(Schedule.EndType newEndType) {
            if (newEndType == Schedule.EndType.AFTER_TIMES)
                onRepeatEnabled();
        }

        @Override
        public void onRepeatToggled(boolean enabled) {
            if (enabled) {
                onRepeatEnabled();
                boolean old = updating;
                updating = true;
                if (schedule.getRepeatSchedule().getType() == Schedule.RepeatType.ITERATIVE) {
                    radioIterative.setChecked(true);
                } else {
                    radioRelative.setChecked(true);
                }
                updating = old;
            } else {
                if (!radioNoRepeat.isChecked())
                    radioNoRepeat.setChecked(true);
                if (radioEndAfterTimes.isChecked()) {
                    radioEndOnDate.setChecked(true);
                    radioEndAfterTimes.setChecked(false);
                }
            }
        }

        @Override
        public void onRepeatTypeChange(Schedule.RepeatType newType) {
            if (newType == Schedule.RepeatType.ITERATIVE) {
                if (!radioIterative.isChecked())
                    radioIterative.setChecked(true);
            } else if (!radioRelative.isChecked()) {
                radioRelative.setChecked(true);
            }
            if (schedule.getEndType() == Schedule.EndType.AFTER_TIMES) {
                schedule.getRepeatSchedule()
                        .endAfterXTimes(Integer.parseInt(editEndDelay.getText().toString()));
            }
        }

        @Override
        public void onRepeatBasisChange(Schedule.RepeatBasis newBasis) {
        }

        @Override
        public void onRepeatDelayChange(int newDelay) {
        }

        private void onRepeatEnabled() {
            updateIterativeSpinner();
            updateRelativeSpinner();
        }
    };
}
