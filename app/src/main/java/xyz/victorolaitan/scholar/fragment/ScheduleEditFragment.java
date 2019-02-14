package xyz.victorolaitan.scholar.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.victorolaitan.scholar.ActivityId;
import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.ScheduleEditCtrl;

import static xyz.victorolaitan.scholar.fragment.FragmentId.SCHEDULE_EDIT_FRAGMENT;

public class ScheduleEditFragment extends Fragment<ScheduleEditCtrl> {
    private boolean popOnViewCreated;
    private ScheduleEditCtrl.ScheduleView scheduleView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (controller == null)
            controller = new ScheduleEditCtrl();
        if (controller.getSchedule() == null) {
            popOnViewCreated = !setSchedule(getDummy(SCHEDULE_EDIT_FRAGMENT, "scheduleView"));
        }
        return inflater.inflate(R.layout.fragment_edit_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (popOnViewCreated) {
            onBackPressed();
        } else {
            controller.init(view);
            controller.updateInfo();
        }
    }

    public boolean setSchedule(ScheduleEditCtrl.ScheduleView scheduleView) {
        if (scheduleView == null) return false;
        this.scheduleView = scheduleView;
        controller.setSchedule(scheduleView);
        return true;
    }

    @Override
    public FragmentId getFragmentId() {
        return SCHEDULE_EDIT_FRAGMENT;
    }

    @Override
    public boolean onHomeUpPressed() {
        return onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY).popFragment(SCHEDULE_EDIT_FRAGMENT);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveDummy(SCHEDULE_EDIT_FRAGMENT, "scheduleView", scheduleView);
        allowDestruction();
    }
}
