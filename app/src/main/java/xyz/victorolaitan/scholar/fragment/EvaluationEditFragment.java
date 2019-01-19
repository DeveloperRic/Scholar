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
import xyz.victorolaitan.scholar.controller.EvaluationEditCtrl;
import xyz.victorolaitan.scholar.controller.RecyclerAdapter;
import xyz.victorolaitan.scholar.model.subject.Evaluation;
import xyz.victorolaitan.scholar.session.Session;

import static xyz.victorolaitan.scholar.fragment.FragmentId.EVALUATION_EDIT_FRAGMENT;

public class EvaluationEditFragment extends Fragment<EvaluationEditCtrl> {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (controller == null) {
            controller = new EvaluationEditCtrl(Session.getSession(),
                    FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY));
        }
        if (savedInstanceState != null) {
            controller.setEvaluation(UUID.fromString(savedInstanceState.getString("courseId")));
        } else if (controller.getEvaluation() == null) {
            controller.setEvaluation((Evaluation) getSavedObject(EVALUATION_EDIT_FRAGMENT, "evaluation"));
        }
        return inflater.inflate(R.layout.fragment_edit_evaluation, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);
        RecyclerView recyclerView = view.findViewById(R.id.tests_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), controller.observableTests,
                R.layout.content_card_evaluation, R.id.class_cardview, R.anim.trans_fade_in);
        recyclerView.setAdapter(adapter);
        controller.setTestsAdapter(adapter);

        recyclerView = view.findViewById(R.id.deliverables_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new RecyclerAdapter(getContext(), controller.observableDeliverables,
                R.layout.content_card_evaluation, R.id.class_cardview, R.anim.trans_fade_in);
        recyclerView.setAdapter(adapter);
        controller.setDeliverablesAdapter(adapter);

        controller.updateInfo();
        controller.refreshCards();
    }

    @Override
    public FragmentId getFragmentId() {
        return EVALUATION_EDIT_FRAGMENT;
    }

    @Override
    public boolean onHomeUpPressed() {
        return onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY).popFragment(EVALUATION_EDIT_FRAGMENT);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveObject(EVALUATION_EDIT_FRAGMENT, "evaluation", controller.getEvaluation());
        allowDestruction();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("courseId", controller.getEvaluation().getCourse().getId().toString());
    }
}
