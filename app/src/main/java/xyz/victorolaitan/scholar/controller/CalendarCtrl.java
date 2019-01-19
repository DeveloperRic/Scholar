package xyz.victorolaitan.scholar.controller;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.fragment.CalendarFragment;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.ScheduleHolder;
import xyz.victorolaitan.scholar.util.Nameable;
import xyz.victorolaitan.scholar.util.Util;

public class CalendarCtrl implements FragmentCtrl {
    private Session parent;

    private Date selectedDate;
    private List<Day> days;

    public List<HomeCard> observableCards;

    private TextView txtMonth;
    private RecyclerView.Adapter cardsAdapter;
    private CalendarFragment.DaysAdapter daysAdapter;

    public CalendarCtrl(Session parent) {
        this.parent = parent;
    }

    public void init(View view) {
        txtMonth = view.findViewById(R.id.calendar_txtMonth);
        days = new ArrayList<>();
        observableCards = new ArrayList<>();
    }

    @Override
    public void updateInfo() {
        updateInfo(selectedDate != null ? selectedDate : new Date());
    }

    private void updateInfo(@NonNull Date recomputeDate) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(recomputeDate);

        String txtMonthText = calendar.getDisplayName(java.util.Calendar.DAY_OF_WEEK,
                java.util.Calendar.LONG, Locale.getDefault())
                + ", " + calendar.getDisplayName(java.util.Calendar.MONTH,
                java.util.Calendar.LONG, Locale.getDefault())
                + " " + calendar.get(java.util.Calendar.DAY_OF_MONTH);
        txtMonth.setText(txtMonthText);

        recomputeDays(recomputeDate);
        selectedDate = recomputeDate;
        for (Day day : days)
            day.updateTextView();

        daysAdapter.notifyDataSetChanged();

        List<ScheduleHolder> modelList = new ArrayList<>();
        for (Nameable n : parent.getCalendar().filterRecursively(recomputeDate)) {
            if (!(n instanceof ScheduleHolder))
                continue;

            modelList.add((ScheduleHolder) n);
        }
        Util.initHomeCardList(observableCards, modelList);
        Util.sortList(observableCards);
        cardsAdapter.notifyDataSetChanged();
    }

    private void recomputeDays(@NonNull Date date) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();

        boolean sameMonth = false;
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);

            calendar.setTime(date);
            sameMonth = calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month;
        }

        TextView[] textViews = null;
        if (sameMonth) {
            textViews = new TextView[days.size()];
            for (int i = 0; i < days.size(); i++) {
                textViews[i] = days.get(i).textView;
            }
        }

        days.clear();

        days.add(new Day(null, "M", Day.DAY_SPECIFIER));
        days.add(new Day(null, "T", Day.DAY_SPECIFIER));
        days.add(new Day(null, "W", Day.DAY_SPECIFIER));
        days.add(new Day(null, "T", Day.DAY_SPECIFIER));
        days.add(new Day(null, "F", Day.DAY_SPECIFIER));
        days.add(new Day(null, "S", Day.DAY_SPECIFIER));
        days.add(new Day(null, "S", Day.DAY_SPECIFIER));

        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayOWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOWeek < 1)
            dayOWeek = 7;
        for (int i = 1; i < dayOWeek; i++) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, -1);
            days.add(7, new Day(calendar.getTime(),
                    String.valueOf(calendar.get(java.util.Calendar.DAY_OF_MONTH)), Day.OUT_OF_MONTH));
        }

        calendar.setTime(date);
        int daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        for (int i = 1; i < daysInMonth; i++) {
            calendar.set(java.util.Calendar.DAY_OF_MONTH, i);
            days.add(new Day(calendar.getTime(), String.valueOf(i), Day.NORMAL));
        }

        dayOWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOWeek < 1)
            dayOWeek = 7;
        for (int i = dayOWeek + 1; i <= 7; i++) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
            days.add(new Day(calendar.getTime(),
                    String.valueOf(calendar.get(java.util.Calendar.DAY_OF_MONTH)), Day.OUT_OF_MONTH));
        }

        if (sameMonth) {
            for (int i = 0; i < textViews.length; i++) {
                days.get(i).setTextView(textViews[i]);
            }
        }
    }

    public void setCardsAdapter(RecyclerView.Adapter cardsAdapter) {
        this.cardsAdapter = cardsAdapter;
    }

    public void setDaysAdapter(CalendarFragment.DaysAdapter daysAdapter) {
        this.daysAdapter = daysAdapter;
    }

    public List<Day> getDays() {
        return days;
    }

    public class Day {
        private static final int NORMAL = 0;
        private static final int DAY_SPECIFIER = 1;
        private static final int OUT_OF_MONTH = 2;

        private int identifier;
        private Date date;
        private String text;
        private TextView textView;

        private Day(Date date, String text, final int identifier) {
            this.date = date;
            this.text = text;
            this.identifier = identifier;
        }

        public void setTextView(TextView textView) {
            this.textView = textView;
            if (identifier != DAY_SPECIFIER) {
                this.textView.setOnClickListener(v -> Day.this.onClick());
            }
            updateTextView();
        }

        private void updateTextView() {
            if (textView == null) {
                return;
            }

            textView.setText(text);
            int textColorId = R.color.colorTextPrimary;

            if (identifier == DAY_SPECIFIER) {
                textColorId = R.color.colorTextSecondary;
            } else if (identifier == OUT_OF_MONTH) {
                textColorId = R.color.colorTextMuted;
            } else {
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.setTime(new Date());
                if (Integer.valueOf(text) == calendar.get(java.util.Calendar.DAY_OF_MONTH)) {
                    textColorId = R.color.colorPrimary;
                }
            }
            textView.setTextColor(textView.getResources().getColor(textColorId));

            if (CalendarCtrl.this.selectedDate.equals(this.date)) {
                textView.setBackgroundColor(
                        textView.getResources().getColor(R.color.colorPrimaryLight));
            } else {
                textView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        void onClick() {
            updateInfo(date);
        }
    }
}
