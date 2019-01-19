package xyz.victorolaitan.scholar.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import xyz.victorolaitan.scholar.ActivityId;
import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.CourseEditCtrl;
import xyz.victorolaitan.scholar.controller.RecyclerAdapter;
import xyz.victorolaitan.scholar.model.subject.Course;
import xyz.victorolaitan.scholar.session.Session;

import static xyz.victorolaitan.scholar.fragment.FragmentId.COURSE_EDIT_FRAGMENT;

public class CourseEditFragment extends Fragment<CourseEditCtrl> {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (controller == null)
            controller = new CourseEditCtrl(Session.getSession(),
                    FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY));
        if (savedInstanceState != null) {
            controller.setCourse(UUID.fromString(savedInstanceState.getString("courseId")));
        } else if (controller.getCourse() == null) {
            controller.setCourse((Course) getSavedObject(COURSE_EDIT_FRAGMENT, "course"));
        }
        return inflater.inflate(R.layout.fragment_edit_course, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);
        RecyclerView homeRecycler = view.findViewById(R.id.classes_recycler);
        homeRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        homeRecycler.setItemAnimator(new DefaultItemAnimator());
        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), controller.observableCards,
                R.layout.content_card_class, R.id.class_cardview, R.anim.trans_fade_in);
        homeRecycler.setAdapter(adapter);
        controller.setClassesAdapter(adapter);

        controller.updateInfo();
        controller.refreshCards();
    }

    @Override
    public FragmentId getFragmentId() {
        return COURSE_EDIT_FRAGMENT;
    }

    @Override
    public boolean onHomeUpPressed() {
        return onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY).popFragment(COURSE_EDIT_FRAGMENT);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveObject(COURSE_EDIT_FRAGMENT, "course", controller.getCourse());
        allowDestruction();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("courseId", controller.getCourse().getId().toString());
    }
}
