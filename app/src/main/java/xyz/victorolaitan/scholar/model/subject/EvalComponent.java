package xyz.victorolaitan.scholar.model.subject;

import java.util.Date;

import xyz.victorolaitan.scholar.util.ScheduleHolder;
import xyz.victorolaitan.scholar.util.Observable;

public interface EvalComponent extends ScheduleHolder, Observable {

    /**
     * @param range inclusive of today i.e.) check [today, today + range - 1]
     * @return true if this schedule occurs within the range, false otherwise
     */
    default boolean dueInXDays(int range) {
        return getSchedule().occursOn(new Date()) || getSchedule().occursInXDays(range - 1);
    }
}
