package xyz.victorolaitan.scholar.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.victorolaitan.scholar.ActivityId;
import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.ScheduleEditCtrl;
import xyz.victorolaitan.scholar.util.Schedule;

import static xyz.victorolaitan.scholar.fragment.FragmentId.SCHEDULE_EDIT_FRAGMENT;

public class ScheduleEditFragment extends Fragment<ScheduleEditCtrl> {
    private boolean popOnViewCreated;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (controller == null)
            controller = new ScheduleEditCtrl();
        if (controller.getSchedule() == null) {
            controller.setSchedule((Schedule) getSavedObject(SCHEDULE_EDIT_FRAGMENT, "schedule"));
            popOnViewCreated = controller.getSchedule() == null;
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
        saveObject(SCHEDULE_EDIT_FRAGMENT, "test", controller.getSchedule());
        allowDestruction();
    }
}
