package xyz.victorolaitan.scholar.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.CalendarCtrl;
import xyz.victorolaitan.scholar.controller.RecyclerAdapter;
import xyz.victorolaitan.scholar.session.Session;

public class CalendarFragment extends Fragment<CalendarCtrl> {
    private CalendarCtrl controller;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (controller == null) {
            controller = new CalendarCtrl(Session.getSession());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);

        RecyclerView calendarRecycler = view.findViewById(R.id.calendar_recycler);
        calendarRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        calendarRecycler.setItemAnimator(new DefaultItemAnimator());

        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), controller.observableCards,
                R.layout.content_card_home_event, R.id.home_cardview, R.anim.trans_fade_in);
        calendarRecycler.setAdapter(adapter);
        controller.setCardsAdapter(adapter);

        GridView gridView = view.findViewById(R.id.calendar_gridview);

        DaysAdapter daysAdapter = new DaysAdapter(getContext(), controller);
        gridView.setAdapter(daysAdapter);
        controller.setDaysAdapter(daysAdapter);

        controller.updateInfo();
    }

    public void setController(CalendarCtrl controller) {
        this.controller = controller;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller = null;
    }

    @Override
    public FragmentId getFragmentId() {
        return FragmentId.CALENDAR_FRAGMENT;
    }

    @Override
    public boolean onHomeUpPressed() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public class DaysAdapter extends BaseAdapter {

        private final Context mContext;
        CalendarCtrl ctrl;

        DaysAdapter(Context context, CalendarCtrl ctrl) {
            this.mContext = context;
            this.ctrl = ctrl;
        }

        @Override
        public int getCount() {
            return ctrl.getDays().size();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return ctrl.getDays().get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View gridView;

            if (convertView == null) {
                gridView = LayoutInflater.from(mContext)
                        .inflate(R.layout.content_calendar_day, parent, false);
                ctrl.getDays().get(position)
                        .setTextView(gridView.findViewById(R.id.calendar_gridview_txtDay));
            } else {
                gridView = convertView;
            }
            return gridView;
        }

    }

}
