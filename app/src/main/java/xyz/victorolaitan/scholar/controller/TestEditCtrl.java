package xyz.victorolaitan.scholar.controller;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.UUID;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.fragment.FragmentActivity;
import xyz.victorolaitan.scholar.fragment.FragmentId;
import xyz.victorolaitan.scholar.model.subject.Test;
import xyz.victorolaitan.scholar.session.DatabaseLink;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.ScheduleChangeListener;
import xyz.victorolaitan.scholar.util.TextChangeListener;
import xyz.victorolaitan.scholar.util.Util;

public class TestEditCtrl implements ModelCtrl {
    private Session parent;
    private FragmentActivity activity;
    private Test test;

    private TextView txtSubject;
    private TextView txtCourse;
    private TextView txtName;
    private TextView txtDate;
    private EditText txtEditName;
    private EditText txtEditDesc;

    public TestEditCtrl(Session parent, FragmentActivity activity) {
        this.parent = parent;
        this.activity = activity;
    }

    public void init(View view) {
        txtSubject = view.findViewById(R.id.editTest_txtSubject);
        txtCourse = view.findViewById(R.id.editTest_txtCourse);
        txtName = view.findViewById(R.id.editTest_txtName);
        txtDate = view.findViewById(R.id.editSchedule_txtDate);

        txtEditName = view.findViewById(R.id.editTest_editName);
        txtEditName.addTextChangedListener((TextChangeListener) (s, start, before, count) -> {
            test.setName(s.toString());
            txtName.setText(test.getName());
        });

        txtEditDesc = view.findViewById(R.id.editTest_editDesc);
        txtEditDesc.addTextChangedListener(
                (TextChangeListener) (s, start, before, count) -> test.setDescription(s.toString()));

        View.OnClickListener dateEditClickListener = v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(test.getSchedule().getEnd());
            Util.newDatePicker(view, cal, test.getSchedule(), Util.SCHEDULE_START, true).show();
        };
        view.findViewById(R.id.editSchedule_changeDate).setOnClickListener(dateEditClickListener);
        txtDate.setOnClickListener(dateEditClickListener);
        view.findViewById(R.id.editSchedule_makeRecurring).setOnClickListener(v ->
                activity.pushFragment(FragmentId.SCHEDULE_EDIT_FRAGMENT, test.getSchedule()));

        test.getSchedule().addChangeListener((ScheduleChangeListener) () -> this);
    }

    @Override
    public void duplicateModel() {
        test.getEvaluation().newTest(test.getName(), test.getDescription(), test.getSchedule());
    }

    @Override
    public void deleteModel() {
        test.getEvaluation().removeTest(test);
    }

    @Override
    public boolean postModel(DatabaseLink database) {
        return database.postEvaluation(test.getEvaluation());
    }

    @Override
    public void updateInfo() {
        txtSubject.setText(test.getEvaluation().getCourse().getSubject().getName());
        txtCourse.setText(test.getEvaluation().getCourse().getName());
        if (test.getName() != null)
            txtName.setText(test.getName());
        txtDate.setText(Util.formatDate(test.getSchedule()));
        txtEditName.setText(test.getName());
        txtEditDesc.setText(test.getDescription());
    }

    public Test getTest() {
        return test;
    }

    public void setTest(UUID testId) {
        setTest((Test) parent.getCalendar().search(testId));
    }

    public void setTest(Test aClass) {
        this.test = aClass;
    }
}
