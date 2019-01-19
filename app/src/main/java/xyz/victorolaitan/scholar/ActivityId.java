package xyz.victorolaitan.scholar;

public enum ActivityId {
    MAIN_ACTIVITY(R.layout.activity_main),
    SUBJECTS_ACTIVITY(R.layout.activity_subjects);

    private int layoutId;

    ActivityId(int layoutId) {
        this.layoutId = layoutId;
    }

    public int getLayoutId() {
        return layoutId;
    }
}
