package xyz.victorolaitan.scholar.controller;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.session.DatabaseLink;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.Util;

public class ScheduleEditCtrl implements ModelCtrl {
    private ScheduleView scheduleView;
    private Schedule schedule;
    private boolean updating, initIterative, initRelative;

    private View parentView;
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
    private int[] repeatDays = new int[0];

    private void checkRepeatNotNull() {
        if (!schedule.hasRepeatSchedule()) {
            schedule.initRepeatSchedule();
        }
    }

    @Override
    public void init(View view) {
        updating = true;
        parentView = view;

        spinnerIterative = view.findViewById(R.id.editSchedule_iterativeSpinner);
        spinnerRelative = view.findViewById(R.id.editSchedule_relativeSpinner);

        radioNoRepeat = view.findViewById(R.id.editSchedule_radioNoRepeat);
        radioNoRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!updating) {
                    schedule.cancelRepeatSchedule();
                    parentView.findViewById(R.id.editSchedule_txtRepeatDays).setVisibility(View.GONE);
                    parentView.findViewById(R.id.editSchedule_repeatDays).setVisibility(View.GONE);
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
                    schedule.setRepeatToIterative();
                    schedule.setRepeatIterativeDelay(schedule.getRepeatIterativeDelay());
                    parentView.findViewById(R.id.editSchedule_txtRepeatDays).setVisibility(View.GONE);
                    parentView.findViewById(R.id.editSchedule_repeatDays).setVisibility(View.GONE);
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
                    if (schedule.getRepeatBasis() == Schedule.RepeatBasis.DAILY) {
                        schedule.setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
                    }
                    if (schedule.getRepeatType() != Schedule.RepeatType.RELATIVE) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(schedule.getStart());
                        repeatDays = new int[]{calendar.get(Calendar.DAY_OF_WEEK)};
                        schedule.setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
                        schedule.setRepeatToRelative(calendar.get(Calendar.DAY_OF_WEEK));
                    }
                    boolean isWeekly = schedule.getRepeatBasis() == Schedule.RepeatBasis.WEEKLY;
                    parentView.findViewById(R.id.editSchedule_txtRepeatDays).setVisibility(isWeekly ? View.VISIBLE : View.GONE);
                    parentView.findViewById(R.id.editSchedule_repeatDays).setVisibility(isWeekly ? View.VISIBLE : View.GONE);
                    updateRelativeSpinner();
                    paintDayTextViews(setDayTextViewListeners(getDayTextViews()));
                }
                radioNoRepeat.setChecked(false);
                radioIterative.setChecked(false);
            }
        });
        radioEndOnDate = view.findViewById(R.id.editSchedule_radioEndOnDay);
        radioEndOnDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!updating)
                    schedule.setEnd(schedule.getEnd());
                radioEndAfterTimes.setChecked(false);
            }
        });
        radioEndAfterTimes = view.findViewById(R.id.editSchedule_radioEndAfterTimes);
        radioEndAfterTimes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!updating)
                    schedule.endAfterXTimes(Integer.parseInt(editEndDelay.getText().toString()));
                radioEndOnDate.setChecked(false);
            }
        });

        txtStart = view.findViewById(R.id.editSchedule_txtStart);
        txtStart.setOnClickListener(v -> changeStart(view));
        txtEndDate = view.findViewById(R.id.editSchedule_txtEndDate);
        txtEndDate.setOnClickListener(v -> changeEnd(view));
        view.findViewById(R.id.editSchedule_changeStart).setOnClickListener(v -> changeStart(view));
        view.findViewById(R.id.editSchedule_changeEndDate).setOnClickListener(v -> changeEnd(view));

        editRepeatDelay = view.findViewById(R.id.editSchedule_editRepeatDelay);
        if (schedule.hasRepeatSchedule() && schedule.getRepeatType() == Schedule.RepeatType.ITERATIVE) {
            editRepeatDelay.setText(String.valueOf(schedule.getRepeatIterativeDelay()));
        } else {
            editRepeatDelay.setText("1");
        }
        editRepeatDelay.addTextChangedListener((TextListener) s -> {
            if (s.length() > 0) {
                checkRepeatNotNull();
                schedule.setRepeatToIterative();
                schedule.setRepeatIterativeDelay(Integer.parseInt(editRepeatDelay.getText().toString()));
            }
        });

        editEndDelay = view.findViewById(R.id.editSchedule_editEndDelay);
        editEndDelay.addTextChangedListener((TextListener) s -> {
            if (s.length() > 0 && !updating) {
                checkRepeatNotNull();
                schedule.endAfterXTimes(Integer.parseInt(editEndDelay.getText().toString()));
                radioEndAfterTimes.toggle();
            }
        });

        if (schedule.getEndType() == Schedule.EndType.ON_DATE) {
            radioEndOnDate.setChecked(true);
            radioEndAfterTimes.setChecked(false);
        } else {
            radioEndOnDate.setChecked(false);
            radioEndAfterTimes.setChecked(true);
            editEndDelay.setText(String.valueOf(schedule.getEndAfterTimes()));
        }

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
        if (!schedule.hasRepeatSchedule()) {
            if (!radioNoRepeat.isChecked()) {
                radioNoRepeat.setChecked(true);
            }
        } else if (schedule.getRepeatType() == Schedule.RepeatType.ITERATIVE) {
            if (!radioIterative.isChecked()) {
                radioIterative.setChecked(true);
            }
        } else if (!radioRelative.isChecked()) {
            radioRelative.setChecked(true);
        }
        radioEndAfterTimes.setEnabled(schedule.hasRepeatSchedule());
        updateIterativeSpinner();
        updateRelativeSpinner();
        boolean isWeekly = schedule.hasRepeatSchedule()
                && schedule.getRepeatBasis() == Schedule.RepeatBasis.WEEKLY;
        parentView.findViewById(R.id.editSchedule_txtRepeatDays).setVisibility(isWeekly ? View.VISIBLE : View.GONE);
        parentView.findViewById(R.id.editSchedule_repeatDays).setVisibility(isWeekly ? View.VISIBLE : View.GONE);
        if (isWeekly) paintDayTextViews(setDayTextViewListeners(getDayTextViews()));

        if (schedule.getEndType() == Schedule.EndType.ON_DATE) {
            radioEndOnDate.setChecked(true);
            radioEndAfterTimes.setChecked(false);
            editEndDelay.setText(String.valueOf(schedule.getMaxOccurrences()));
        } else {
            radioEndOnDate.setChecked(false);
            radioEndAfterTimes.setChecked(true);
            editEndDelay.setText(String.valueOf(schedule.getEndAfterTimes()));
        }
        updating = false;
    }

    private void updateIterativeSpinner() {
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
                            schedule.hasRepeatSchedule() && schedule.getRepeatType() == Schedule.RepeatType.ITERATIVE
                                    ? schedule.getRepeatIterativeDelay() : 1));
        }
        spinnerAdapter.notifyDataSetChanged();

        if (schedule.hasRepeatSchedule()) {
            spinnerIterative.setSelection(schedule.getRepeatBasis().getIndex());
        }

        initIterative = false;
        spinnerIterative.setOnItemSelectedListener((SpinnerListener) (parent, view, position, id) -> {
            if (!initIterative) {
                initIterative = true;
                return;
            }
            checkRepeatNotNull();
            schedule.setRepeatToIterative();
            if (position == 0)
                schedule.setRepeatBasis(Schedule.RepeatBasis.DAILY);
            else if (position == 1)
                schedule.setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
            else if (position == 2)
                schedule.setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
            else
                schedule.setRepeatBasis(Schedule.RepeatBasis.YEARLY);
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

        if (!schedule.hasRepeatSchedule() || schedule.getRepeatType() != Schedule.RepeatType.RELATIVE) {

            spinnerAdapter.add(spinnerRelative.getResources().getString(R.string.editSchedule_repeatSpinnerSelectDays));

        } else if (schedule.getRepeatBasis() != Schedule.RepeatBasis.WEEKLY || !schedule.hasRepeatRelativeSelections()) {

            spinnerAdapter.add(spinnerRelative.getResources().getString(R.string.editSchedule_repeatSpinnerDayOWeekFormat,
                    calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())));

        } else if (schedule.getRepeatRelativeSelections().length == 1) {
            calendar.set(Calendar.DAY_OF_WEEK, schedule.getRepeatRelativeSelections()[0]);

            spinnerAdapter.add(spinnerRelative.getResources().getString(R.string.editSchedule_repeatSpinnerDayOWeekFormat,
                    calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())));

            calendar.setTime(schedule.getStart());
        } else {
            StringBuilder sb = new StringBuilder();
            int[] selections = schedule.getRepeatRelativeSelections();

            for (int i = 0; i < selections.length; i++) {
                calendar.set(Calendar.DAY_OF_WEEK, selections[i]);
                sb.append(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
                if (i + 1 < selections.length) sb.append(", ");
            }

            spinnerAdapter.add(spinnerRelative.getResources().getString(R.string.editSchedule_repeatSpinnerDayOWeekFormat,
                    sb.toString()));
            calendar.setTime(schedule.getStart());
        }

        int dayOWeekOMonth = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        spinnerAdapter.add(spinnerRelative.getResources().getString(R.string.editSchedule_repeatSpinnerItemFormat,
                dayOWeekOMonth,
                firstSecondEtc[dayOWeekOMonth <= 3 ? dayOWeekOMonth - 1 : 3],
                calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()),
                spinnerRelative.getResources().getQuantityString(R.plurals.editSchedule_basisMonth, 1)));

        int weekOMonth = calendar.get(Calendar.WEEK_OF_MONTH);
        spinnerAdapter.add(spinnerRelative.getResources().getString(R.string.editSchedule_repeatSpinnerItemFormat,
                weekOMonth,
                firstSecondEtc[weekOMonth != 4 ? weekOMonth - 1 : 3],
                spinnerRelative.getResources().getQuantityString(R.plurals.editSchedule_basisWeek, 1),
                spinnerRelative.getResources().getQuantityString(R.plurals.editSchedule_basisMonth, 1)));

        spinnerAdapter.notifyDataSetChanged();

        if (schedule.hasRepeatSchedule()) {
            if (schedule.getRepeatType() == Schedule.RepeatType.RELATIVE) {
                if (schedule.getRepeatBasis() == Schedule.RepeatBasis.WEEKLY) {
                    spinnerRelative.setSelection(0);
                } else if (schedule.getRepeatBasis() == Schedule.RepeatBasis.MONTHLY) {
                    spinnerRelative.setSelection(1);
                } else if (schedule.getRepeatBasis() == Schedule.RepeatBasis.YEARLY) {
                    spinnerRelative.setSelection(2);
                }
            }
        }

        initRelative = false;
        spinnerRelative.setOnItemSelectedListener((SpinnerListener) (parent, view, position, id) -> {
            if (!initRelative) {
                initRelative = true;
                return;
            }
            checkRepeatNotNull();
            if (position == 0) {
                if (schedule.getRepeatRelativeSelections().length <= 1) {
                    schedule.clearRepeatRelativeSelections();
                    int dayOWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                    if (dayOWeek <= 0) dayOWeek = 7;
                    schedule.setRepeatToRelative(dayOWeek);
                }
                schedule.setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
            } else if (position == 1) {
                schedule.clearRepeatRelativeSelections();
                schedule.setRepeatToRelative(dayOWeekOMonth);
                schedule.setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
//            } else {
//                schedule.setRepeatToRelative(calendar.get(DAY));
//                schedule.setRepeatBasis(Schedule.RepeatBasis.MONTHLY);
            }
        });
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleView scheduleView) {
        this.scheduleView = scheduleView;
        this.schedule = scheduleView.getSchedule();
    }

    @Override
    public void duplicateModel() {
    }

    @Override
    public void deleteModel() {
    }

    @Override
    public boolean postModel(DatabaseLink database) {
        return scheduleView.postModel(database);
    }

    public interface ScheduleView {
        Schedule getSchedule();

        boolean postModel(DatabaseLink database);
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

    private boolean repeatDaysContains(int i) {
        for (int day : repeatDays) {
            if (day == i) return true;
        }
        return false;
    }

    private TextView[] getDayTextViews() {
        return new TextView[]{
                parentView.findViewById(R.id.editSchedule_repeatMon),
                parentView.findViewById(R.id.editSchedule_repeatTue),
                parentView.findViewById(R.id.editSchedule_repeatWed),
                parentView.findViewById(R.id.editSchedule_repeatThu),
                parentView.findViewById(R.id.editSchedule_repeatFri),
                parentView.findViewById(R.id.editSchedule_repeatSat),
                parentView.findViewById(R.id.editSchedule_repeatSun)
        };
    }

    private void paintDayTextViews(TextView[] textViews) {
        int[] colouredIndexes = new int[schedule.getRepeatRelativeSelections().length];
        for (int i = 0; i < schedule.getRepeatRelativeSelections().length; i++) {
            int day = schedule.getRepeatRelativeSelections()[i];
            colouredIndexes[i] = (day - 2) < 0 ? 6 : day - 2;
        }
        outer:
        for (int i = 0; i < 7; i++) {
            for (int colouredIndex : colouredIndexes) {
                if (i == colouredIndex) {
                    textViews[i].setBackgroundColor(parentView.getResources().getColor(R.color.colorPrimary));
                    textViews[i].setTextColor(parentView.getResources().getColor(R.color.colorText));
                    continue outer;
                }
            }
            textViews[i].setBackgroundColor(0);
            textViews[i].setTextColor(parentView.getResources().getColor(R.color.colorTextPrimary));
        }
    }

    private TextView[] setDayTextViewListeners(TextView[] textViews) {
        for (int i = 0; i < textViews.length; i++) {
            int index = i;
            textViews[i].setOnClickListener(v -> {
                int calendarDay = (index + 2) > 7 ? 1 : index + 2;
                int[] newDays;
                if (repeatDaysContains(calendarDay)) {
                    newDays = new int[repeatDays.length - 1];
                    int skippedIndexes = 0;
                    for (int j = 0; j < repeatDays.length; j++) {
                        if (repeatDays[j] != calendarDay) {
                            newDays[j - skippedIndexes] = repeatDays[j];
                        } else {
                            skippedIndexes++;
                        }
                    }
                    textViews[index].setBackgroundColor(0);
                    textViews[index].setTextColor(parentView.getResources().getColor(R.color.colorTextPrimary));
                } else {
                    newDays = new int[repeatDays.length + 1];
                    System.arraycopy(repeatDays, 0, newDays, 0, repeatDays.length);
                    newDays[newDays.length - 1] = calendarDay;
                    textViews[index].setBackgroundColor(parentView.getResources().getColor(R.color.colorPrimary));
                    textViews[index].setTextColor(parentView.getResources().getColor(R.color.colorText));
                }
                if (newDays.length > 0) {
                    Arrays.sort(newDays);
                    schedule.setRepeatToRelative(newDays);
                } else {
                    schedule.setRepeatToRelative(repeatDays);
                }
                schedule.setRepeatBasis(Schedule.RepeatBasis.WEEKLY);
            });
        }
        return textViews;
    }

    private Schedule.ChangeListener scheduleListener = new Schedule.ChangeListener() {
        @Override
        public void onStartChange(Date newStart) {
            if (newStart.after(schedule.getEnd())) {
                txtStart.setTextColor(Color.RED);
            } else {
                txtStart.setTextColor(txtStart.getResources().getColor(R.color.colorTextSecondary));
            }
            txtStart.setText(Util.formatDate(newStart));
        }

        @Override
        public void onEndChange(Date newEnd) {
            if (newEnd.before(schedule.getStart())) {
                txtEndDate.setTextColor(Color.RED);
            } else {
                txtEndDate.setTextColor(txtEndDate.getResources().getColor(R.color.colorTextSecondary));
            }
            txtEndDate.setText(Util.formatDate(newEnd));
        }

        @Override
        public void onEndTypeChange(Schedule.EndType endType, int endAfterTimes) {
            if (endType == Schedule.EndType.ON_DATE) {
                if (!radioEndOnDate.isChecked()) radioEndOnDate.setChecked(true);
                radioEndAfterTimes.setChecked(false);
            } else {
                radioEndOnDate.setChecked(false);
                if (!radioEndAfterTimes.isChecked()) radioEndAfterTimes.setChecked(true);
                editEndDelay.setText(String.valueOf(endAfterTimes));
            }
        }

        @Override
        public void onRepeatToggled(boolean enabled) {
            if (enabled) {
                onRepeatEnabled();
                boolean old = updating;
                updating = true;
                if (schedule.getRepeatType() == Schedule.RepeatType.ITERATIVE) {
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
            radioEndAfterTimes.setEnabled(enabled);
        }

        @Override
        public void onRepeatTypeChange(Schedule.RepeatType newType) {
            if (newType == Schedule.RepeatType.ITERATIVE) {
                if (!radioIterative.isChecked())
                    radioIterative.setChecked(true);
            } else {
                if (!radioRelative.isChecked()) {
                    radioRelative.setChecked(true);
                }
                updateRelativeSpinner();
            }
        }

        @Override
        public void onRepeatBasisChange(Schedule.RepeatBasis newBasis) {
            boolean isWeekly =
                    schedule.getRepeatType() == Schedule.RepeatType.RELATIVE
                            && newBasis == Schedule.RepeatBasis.WEEKLY;
            parentView.findViewById(R.id.editSchedule_txtRepeatDays).setVisibility(isWeekly ? View.VISIBLE : View.GONE);
            parentView.findViewById(R.id.editSchedule_repeatDays).setVisibility(isWeekly ? View.VISIBLE : View.GONE);
            if (isWeekly) {
                paintDayTextViews(getDayTextViews());
            }
        }

        @Override
        public void onRepeatIterativeDelayChange(int newDelay) {
        }

        @Override
        public void onRepeatRelativeSelectionChange(int[] selections) {
            if (schedule.getRepeatBasis() == Schedule.RepeatBasis.WEEKLY) {
                repeatDays = selections;
                paintDayTextViews(getDayTextViews());
            }
        }

        private void onRepeatEnabled() {
            updateIterativeSpinner();
            updateRelativeSpinner();
        }
    };
}
