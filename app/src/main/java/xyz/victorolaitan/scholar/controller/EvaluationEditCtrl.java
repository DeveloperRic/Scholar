package xyz.victorolaitan.scholar.controller;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.fragment.FragmentId;
import xyz.victorolaitan.scholar.model.subject.Course;
import xyz.victorolaitan.scholar.model.subject.Deliverable;
import xyz.victorolaitan.scholar.model.subject.EvalComponent;
import xyz.victorolaitan.scholar.model.subject.Evaluation;
import xyz.victorolaitan.scholar.model.subject.Test;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.Comparable;
import xyz.victorolaitan.scholar.fragment.FragmentActivity;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.ScholarModel;
import xyz.victorolaitan.scholar.util.Util;

public class EvaluationEditCtrl implements FragmentCtrl {
    private Session parent;
    private FragmentActivity activity;
    private Evaluation evaluation;

    public List<EvalCard> observableTests;
    public List<EvalCard> observableDeliverables;

    private TextView txtSubject;
    private TextView txtCourse;
    private RecyclerAdapter testsAdapter;
    private RecyclerAdapter deliverablesAdapter;

    public EvaluationEditCtrl(Session parent, FragmentActivity activity) {
        this.parent = parent;
        this.activity = activity;
        observableTests = new ArrayList<>();
        observableDeliverables = new ArrayList<>();
    }

    public void init(View view) {
        txtSubject = view.findViewById(R.id.editEval_txtSubject);
        txtCourse = view.findViewById(R.id.editEval_txtCourse);

        view.findViewById(R.id.editEval_addTest).setOnClickListener(v ->
                activity.pushFragment(FragmentId.TEST_EDIT_FRAGMENT,
                        evaluation.newTest("", "", new Schedule())));
        view.findViewById(R.id.editEval_addDeliv).setOnClickListener(v ->
                activity.pushFragment(FragmentId.DELIVERABLE_EDIT_FRAGMENT,
                        evaluation.newDeliverable("", "", new Schedule())));
    }

    @Override
    public void updateInfo() {
        txtSubject.setText(evaluation.getCourse().getSubject().getName());
        txtCourse.setText(evaluation.getCourse().getName());
    }

    public void refreshCards() {
        observableTests.clear();
        for (Test test : evaluation.getTests()) {
            observableTests.add(new EvalCard(test));
        }
        Util.sortList(observableTests);
        testsAdapter.notifyDataSetChanged();

        observableDeliverables.clear();
        for (Deliverable deliverable : evaluation.getDeliverables()) {
            observableDeliverables.add(new EvalCard(deliverable));
        }
        Util.sortList(observableDeliverables);
        deliverablesAdapter.notifyDataSetChanged();
    }

    public void setTestsAdapter(RecyclerAdapter testsAdapter) {
        this.testsAdapter = testsAdapter;
    }

    public void setDeliverablesAdapter(RecyclerAdapter deliverablesAdapter) {
        this.deliverablesAdapter = deliverablesAdapter;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(UUID courseId) {
        ScholarModel result = parent.getCalendar().search(courseId);
        if (result != null)
            setEvaluation(((Course) result).getEvaluation());
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public final class EvalCard implements Comparable, RecyclerCard {
        private EvalComponent component;

        private CardView cv;
        private TextView txtDate;
        private TextView txtTitle;

        private EvalCard(EvalComponent component) {
            this.component = component;
        }

        @Override
        public void attachLayoutViews(View layout, CardView cv) {
            this.cv = cv;
            txtDate = layout.findViewById(R.id.evals_cardview_date);
            txtTitle = layout.findViewById(R.id.evals_cardview_title);
            cv.setOnClickListener(v -> {
                if (component instanceof Test) {
                    activity.pushFragment(FragmentId.TEST_EDIT_FRAGMENT, component);
                } else {
                    activity.pushFragment(FragmentId.DELIVERABLE_EDIT_FRAGMENT, component);
                }
            });
        }

        @Override
        public void updateInfo() {
            if (component.dueInXDays(4)) {
                cv.setBackgroundColor(cv.getContext().getResources().getColor(R.color.colorPriorityHigh));
            } else if (component.dueInXDays(8)) {
                cv.setBackgroundColor(cv.getContext().getResources().getColor(R.color.colorPriorityMedium));
            }
            txtDate.setText(DateFormat.getDateInstance().format(component.getSchedule().getStart()));
            txtTitle.setText(component.getName());
        }

        @Override
        public int getCompareType() {
            return COMPLEX;
        }

        @Override
        public java.lang.Comparable getCompareObject() {
            return component.getSchedule();
        }
    }
}
