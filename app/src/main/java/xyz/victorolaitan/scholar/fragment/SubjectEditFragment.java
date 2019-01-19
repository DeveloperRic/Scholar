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
import xyz.victorolaitan.scholar.controller.RecyclerAdapter;
import xyz.victorolaitan.scholar.controller.SubjectEditCtrl;
import xyz.victorolaitan.scholar.model.subject.Subject;
import xyz.victorolaitan.scholar.session.Session;

import static xyz.victorolaitan.scholar.fragment.FragmentId.SUBJECT_EDIT_FRAGMENT;

public class SubjectEditFragment extends Fragment<SubjectEditCtrl> {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (controller == null)
            controller = new SubjectEditCtrl(
                    Session.getSession(),
                    FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY));
        if (savedInstanceState != null) {
            controller.setSubject(UUID.fromString(savedInstanceState.getString("subjectId")));
        } else if (controller.getSubject() == null) {
            controller.setSubject((Subject) getSavedObject(SUBJECT_EDIT_FRAGMENT, "subject"));
        }
        return inflater.inflate(R.layout.fragment_edit_subject, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);
        RecyclerView homeRecycler = view.findViewById(R.id.subjects_recycler);
        homeRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        homeRecycler.setItemAnimator(new DefaultItemAnimator());
        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), controller.observableCards,
                R.layout.content_card_subject, R.id.home_cardview, R.anim.trans_fade_in);
        homeRecycler.setAdapter(adapter);
        controller.setCoursesAdapter(adapter);

        controller.updateInfo();
        controller.refreshCards();
    }

    @Override
    public FragmentId getFragmentId() {
        return SUBJECT_EDIT_FRAGMENT;
    }


    @Override
    public boolean onHomeUpPressed() {
        return onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY).popFragment(SUBJECT_EDIT_FRAGMENT);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveObject(SUBJECT_EDIT_FRAGMENT, "subject", controller.getSubject());
        allowDestruction();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("subjectId", controller.getSubject().getSubjectId().toString());
    }
}
