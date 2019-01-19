package xyz.victorolaitan.scholar.fragment;

import xyz.victorolaitan.scholar.ActivityId;

public enum FragmentId {
    HOME_FRAGMENT(0, ActivityId.MAIN_ACTIVITY),
    CALENDAR_FRAGMENT(1, ActivityId.MAIN_ACTIVITY),
    EVALUATION_FRAGMENT(2, ActivityId.SUBJECTS_ACTIVITY),
    SUBJECT_LIST_FRAGMENT(3, ActivityId.SUBJECTS_ACTIVITY),
    SUBJECT_EDIT_FRAGMENT(4, ActivityId.SUBJECTS_ACTIVITY),
    COURSE_EDIT_FRAGMENT(5, ActivityId.SUBJECTS_ACTIVITY),
    CLASS_EDIT_FRAGMENT(6, ActivityId.SUBJECTS_ACTIVITY),
    EVALUATION_EDIT_FRAGMENT(7, ActivityId.SUBJECTS_ACTIVITY),
    TEST_EDIT_FRAGMENT(8, ActivityId.SUBJECTS_ACTIVITY),
    DELIVERABLE_EDIT_FRAGMENT(9, ActivityId.SUBJECTS_ACTIVITY),
    SCHEDULE_EDIT_FRAGMENT(10, ActivityId.SUBJECTS_ACTIVITY),
    PERSON_SELECT_FRAGMENT(11, ActivityId.SUBJECTS_ACTIVITY);

    private int id;
    private ActivityId defaultActivityId;

    FragmentId(int id, ActivityId defaultActivityId) {
        this.id = id;
        this.defaultActivityId = defaultActivityId;
    }

    public int getId() {
        return id;
    }

    ActivityId getDefaultActivityId() {
        return defaultActivityId;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
