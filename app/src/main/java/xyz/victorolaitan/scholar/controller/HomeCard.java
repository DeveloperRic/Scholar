package xyz.victorolaitan.scholar.controller;

import android.annotation.SuppressLint;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.util.Comparable;
import xyz.victorolaitan.scholar.util.ScheduleHolder;

public final class HomeCard implements RecyclerCard, Comparable {

    @Override
    public int getCompareType() {
        return COMPLEX;
    }

    @Override
    public java.lang.Comparable getCompareObject() {
        return object;
    }

    public enum CardType {
        NORMAL, TEST, DELIVERABLE
    }

    private CardType type;
    private ScheduleHolder object;
    private TextView txtTitle;
    private TextView txtDate;

    public HomeCard(CardType type, ScheduleHolder object) {
        this.type = type;
        this.object = object;
    }

    @Override
    public void attachLayoutViews(View layout, CardView cv) {
        txtTitle = layout.findViewById(R.id.home_cardview_title);
        txtDate = layout.findViewById(R.id.home_cardview_date);

        if (type == CardType.NORMAL)
            return;

        int bgColour = 0;
        switch (type) {
            case TEST:
                bgColour = R.color.colorPriorityHigh;
                break;
            case DELIVERABLE:
                bgColour = R.color.colorPriorityMedium;
                break;
        }
        cv.setBackgroundColor(cv.getContext().getResources().getColor(bgColour));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateInfo() {
        txtTitle.setText(object.getFancyName());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(object.getSchedule().getStart());
        txtDate.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
    }
}
