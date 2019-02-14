package xyz.victorolaitan.scholar.util;


import java.util.Date;

import xyz.victorolaitan.scholar.controller.ActivityCtrl;

public interface ScheduleChangeListener extends Schedule.ChangeListener {

    ActivityCtrl getCtrl();

    @Override
    default void onStartChange(Date newStart) {
        getCtrl().updateInfo();
    }

    @Override
    default void onEndChange(Date newEnd) {
        getCtrl().updateInfo();
    }

    @Override
    default void onEndTypeChange(Schedule.EndType endType, int endAfterTimes) {
        getCtrl().updateInfo();
    }

    @Override
    default void onRepeatToggled(boolean enabled) {
        getCtrl().updateInfo();
    }

    @Override
    default void onRepeatTypeChange(Schedule.RepeatType newType) {
        getCtrl().updateInfo();
    }

    @Override
    default void onRepeatBasisChange(Schedule.RepeatBasis newBasis) {
        getCtrl().updateInfo();
    }

    @Override
    default void onRepeatIterativeDelayChange(int newDelay) {
        getCtrl().updateInfo();
    }

    @Override
    default void onRepeatRelativeSelectionChange(int[] selections) {
        getCtrl().updateInfo();
    }
}
