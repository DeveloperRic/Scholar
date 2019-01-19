package xyz.victorolaitan.scholar.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.victorolaitan.scholar.ActivityId;
import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.RecyclerAdapter;
import xyz.victorolaitan.scholar.controller.SubjectListCtrl;
import xyz.victorolaitan.scholar.session.Session;

public class SubjectListFragment extends Fragment<SubjectListCtrl> {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (controller == null)
            controller = new SubjectListCtrl(
                    Session.getSession(),
                    FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY));
        return inflater.inflate(R.layout.fragment_subject_list, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init();
        RecyclerView homeRecycler = view.findViewById(R.id.subjects_recycler);
        homeRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        homeRecycler.setItemAnimator(new DefaultItemAnimator());

        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), controller.observableCards,
                R.layout.content_card_subject, R.id.home_cardview, R.anim.trans_fade_in);
        homeRecycler.setAdapter(adapter);
        controller.setSubjectsAdapter(adapter);

        controller.updateInfo();
    }

    public void onNewSubjectClick() {
        controller.onNewSubjectClick();
    }

    @Override
    public FragmentId getFragmentId() {
        return FragmentId.SUBJECT_LIST_FRAGMENT;
    }

    @Override
    public boolean onHomeUpPressed() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
