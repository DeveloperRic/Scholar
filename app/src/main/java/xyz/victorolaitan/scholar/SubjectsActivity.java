package xyz.victorolaitan.scholar;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import xyz.victorolaitan.scholar.controller.ClassEditCtrl;
import xyz.victorolaitan.scholar.controller.CourseEditCtrl;
import xyz.victorolaitan.scholar.controller.DeliverableEditCtrl;
import xyz.victorolaitan.scholar.controller.EvaluationEditCtrl;
import xyz.victorolaitan.scholar.controller.PersonSelectCtrl;
import xyz.victorolaitan.scholar.controller.ScheduleEditCtrl;
import xyz.victorolaitan.scholar.controller.SubjectEditCtrl;
import xyz.victorolaitan.scholar.controller.SubjectListCtrl;
import xyz.victorolaitan.scholar.controller.TestEditCtrl;
import xyz.victorolaitan.scholar.fragment.ClassEditFragment;
import xyz.victorolaitan.scholar.fragment.CourseEditFragment;
import xyz.victorolaitan.scholar.fragment.DeliverableEditFragment;
import xyz.victorolaitan.scholar.fragment.EvaluationEditFragment;
import xyz.victorolaitan.scholar.fragment.Fragment;
import xyz.victorolaitan.scholar.fragment.FragmentActivity;
import xyz.victorolaitan.scholar.fragment.FragmentId;
import xyz.victorolaitan.scholar.fragment.PersonSelectFragment;
import xyz.victorolaitan.scholar.fragment.ScheduleEditFragment;
import xyz.victorolaitan.scholar.fragment.SubjectEditFragment;
import xyz.victorolaitan.scholar.fragment.SubjectListFragment;
import xyz.victorolaitan.scholar.fragment.TestEditFragment;
import xyz.victorolaitan.scholar.model.subject.Class;
import xyz.victorolaitan.scholar.model.subject.Course;
import xyz.victorolaitan.scholar.model.subject.Deliverable;
import xyz.victorolaitan.scholar.model.subject.Evaluation;
import xyz.victorolaitan.scholar.model.subject.Subject;
import xyz.victorolaitan.scholar.model.subject.Test;
import xyz.victorolaitan.scholar.util.Indexable;

import static xyz.victorolaitan.scholar.fragment.FragmentId.CLASS_EDIT_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.COURSE_EDIT_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.DELIVERABLE_EDIT_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.EVALUATION_EDIT_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.PERSON_SELECT_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.SCHEDULE_EDIT_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.SUBJECT_EDIT_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.SUBJECT_LIST_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.TEST_EDIT_FRAGMENT;

public class SubjectsActivity extends FragmentActivity {
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private FloatingActionButton fab;

    public SubjectsActivity() {
        saveInstance(ActivityId.SUBJECTS_ACTIVITY, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);

        Toolbar toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        fab = findViewById(R.id.subjects_addButton);
        pushFragment(SUBJECT_LIST_FRAGMENT);

        fab.setOnClickListener(v -> {
            if (currentFragment() != null && currentFragment().getFragmentId() == SUBJECT_LIST_FRAGMENT) {
                ((SubjectListFragment) currentFragment()).onNewSubjectClick();
            }
        });
    }

    @Override
    protected ActivityId getActivityId() {
        return ActivityId.SUBJECTS_ACTIVITY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void pushFragment(FragmentId fragmentId, Object args[]) {
        Fragment fragment;
        if (fragmentId == SUBJECT_LIST_FRAGMENT) {
            fragment = new SubjectListFragment();
            fragment.setController(new SubjectListCtrl(getSession(), this));
        } else if (fragmentId == SUBJECT_EDIT_FRAGMENT) {
            SubjectEditCtrl ctrl = new SubjectEditCtrl(getSession(), this);
            ctrl.setSubject((Subject) args[0]);
            fragment = new SubjectEditFragment();
            fragment.setController(ctrl);
        } else if (fragmentId == COURSE_EDIT_FRAGMENT) {
            CourseEditCtrl ctrl = new CourseEditCtrl(getSession(), this);
            ctrl.setCourse((Course) args[0]);
            fragment = new CourseEditFragment();
            fragment.setController(ctrl);
        } else if (fragmentId == CLASS_EDIT_FRAGMENT) {
            ClassEditCtrl ctrl = new ClassEditCtrl(getSession(), this);
            ctrl.setTheClass((Class) args[0]);
            fragment = new ClassEditFragment();
            fragment.setController(ctrl);
        } else if (fragmentId == EVALUATION_EDIT_FRAGMENT) {
            EvaluationEditCtrl ctrl = new EvaluationEditCtrl(getSession(), this);
            ctrl.setEvaluation((Evaluation) args[0]);
            fragment = new EvaluationEditFragment();
            fragment.setController(ctrl);
        } else if (fragmentId == TEST_EDIT_FRAGMENT) {
            TestEditCtrl ctrl = new TestEditCtrl(getSession(), this);
            ctrl.setTest((Test) args[0]);
            fragment = new TestEditFragment();
            fragment.setController(ctrl);
        } else if (fragmentId == DELIVERABLE_EDIT_FRAGMENT) {
            DeliverableEditCtrl ctrl = new DeliverableEditCtrl(getSession(), this);
            ctrl.setDeliverable((Deliverable) args[0]);
            fragment = new DeliverableEditFragment();
            fragment.setController(ctrl);
        } else if (fragmentId == SCHEDULE_EDIT_FRAGMENT) {
            ScheduleEditCtrl ctrl = new ScheduleEditCtrl();
            fragment = new ScheduleEditFragment();
            fragment.setController(ctrl);
            ((ScheduleEditFragment) fragment).setSchedule((ScheduleEditCtrl.ScheduleView) args[0]);
        } else if (fragmentId == PERSON_SELECT_FRAGMENT) {
            PersonSelectCtrl ctrl = new PersonSelectCtrl(getSession(), this);
            ctrl.setContext((PersonSelectCtrl.PersonType) args[0], (Indexable) args[1]);
            ctrl.setCallback((PersonSelectCtrl.PersonSelectCallback) args[2]);
            fragment = new PersonSelectFragment();
            fragment.setController(ctrl);
        } else {
            return;
        }
        fragmentStack.push(fragment);
        if (fragmentId != SUBJECT_LIST_FRAGMENT) {
            fab.hide();
        }
        fragmentManager.beginTransaction()
                .replace(R.id.subjects_fragment, currentFragment())
                .addToBackStack(String.valueOf(fragmentId))
                .commit();
    }

    @Override
    public void popFragment(FragmentId fragmentId) {
        fragmentManager.popBackStack(
                String.valueOf(fragmentId), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentStack.pop().destroy();
        if (currentFragment() != null && currentFragment().getFragmentId() == SUBJECT_LIST_FRAGMENT) {
            fab.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_model_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                if (currentFragment() != null) {
                    if (!currentFragment().onOptionsItemSelected(item.getItemId())) {
                        return super.onOptionsItemSelected(item);
                    } else {
                        return true;
                    }
                } else {
                    return super.onOptionsItemSelected(item);
                }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = currentFragment();
        if (fragment != null) {
            if (!fragment.onBackPressed()) {
                super.onBackPressed();
                super.onBackPressed();
            } else if (fragment.getFragmentId() == SUBJECT_LIST_FRAGMENT) {
                fab.show();
            }
        } else {
            fab.show();
            super.onBackPressed();
        }
    }
}
