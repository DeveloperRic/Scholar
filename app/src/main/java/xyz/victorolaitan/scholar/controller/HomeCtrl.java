package xyz.victorolaitan.scholar.controller;

import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.ScheduleHolder;
import xyz.victorolaitan.scholar.util.Nameable;
import xyz.victorolaitan.scholar.util.Util;

public class HomeCtrl implements FragmentCtrl {

    private Session parent;

    private TextView txtDayOfWeek;
    private TextView txtDate;
    private TextView txtMonth;
    private TextView txtSummary;
    public List<HomeCard> observableCards;

    private RecyclerAdapter cardsAdapter;

    public HomeCtrl(Session parent) {
        this.parent = parent;
    }

    public void init(View view) {
        txtDayOfWeek = view.findViewById(R.id.home_txtDayOfWeek);
        txtDate = view.findViewById(R.id.home_txtDate);
        txtMonth = view.findViewById(R.id.home_txtMonth);
        txtSummary = view.findViewById(R.id.home_txtSummary);
        observableCards = new ArrayList<>();
    }

    public void launch(TextView txt) {
        txt.setText(parent.getCalendar().toJSON().toString());
        updateInfo();
    }

    @SuppressWarnings("ConstantConditions")
    public void updateInfo() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(new Date());

        txtDayOfWeek.setText(calendar.getDisplayName(java.util.Calendar.DAY_OF_WEEK,
                java.util.Calendar.SHORT, Locale.getDefault()));
        txtDate.setText(String.valueOf(calendar.get(java.util.Calendar.DAY_OF_MONTH)));
        txtMonth.setText(calendar.getDisplayName(java.util.Calendar.MONTH,
                java.util.Calendar.SHORT, Locale.getDefault()));

        List<ScheduleHolder> modelList = new ArrayList<>();
        HashMap<String, Integer> today = new HashMap<>();
        for (Nameable n : parent.getCalendar().filterRecursively(new Date())) {
            if (!(n instanceof ScheduleHolder))
                continue;

            modelList.add((ScheduleHolder) n);

            if (today.containsKey(n.getShortName())) {
                today.put(n.getShortName(), today.get(n.getShortName()) + 1);
            } else {
                today.put(n.getShortName(), 1);
            }
        }
        Util.initHomeCardList(observableCards, modelList);
        Util.sortList(observableCards);
        cardsAdapter.notifyDataSetChanged();

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String s : today.keySet()) {
            i++;
            int count = today.get(s);
            sb.append(count).append(" ");
            if (count == 1) {
                sb.append(s);
            } else if (s.endsWith("s")) {
                sb.append(s).append("es");
            } else if (s.endsWith("y")) {
                sb.append(s.substring(0, s.length() - 2)).append("ies");
            } else {
                sb.append(s).append("s");
            }
            if (i < today.size())
                sb.append("\n");
        }
        txtSummary.setText(sb.toString());
    }

    public void setCardsAdapter(RecyclerAdapter cardsAdapter) {
        this.cardsAdapter = cardsAdapter;
    }

}
