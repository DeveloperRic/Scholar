package xyz.victorolaitan.scholar.controller;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.UUID;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.fragment.FragmentActivity;
import xyz.victorolaitan.scholar.fragment.FragmentId;
import xyz.victorolaitan.scholar.model.Teacher;
import xyz.victorolaitan.scholar.model.subject.Class;
import xyz.victorolaitan.scholar.session.DatabaseLink;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.ScheduleChangeListener;
import xyz.victorolaitan.scholar.util.TextChangeListener;
import xyz.victorolaitan.scholar.util.Util;

import static xyz.victorolaitan.scholar.fragment.FragmentId.PERSON_SELECT_FRAGMENT;

public class ClassEditCtrl implements ModelCtrl {
    private Session parent;
    private FragmentActivity activity;
    private Class aClass;

    private TextView txtSubject;
    private TextView txtCourse;
    private TextView txtName;
    private TextView txtTeacher;
    private TextView txtDate;
    private EditText txtEditName;

    public ClassEditCtrl(Session parent, FragmentActivity activity) {
        this.parent = parent;
        this.activity = activity;
    }

    public void init(View view) {
        txtSubject = view.findViewById(R.id.editClass_txtSubject);
        txtCourse = view.findViewById(R.id.editClass_txtCourse);
        txtName = view.findViewById(R.id.editClass_txtName);

        txtEditName = view.findViewById(R.id.editClass_editName);
        txtEditName.addTextChangedListener((TextChangeListener) (s, start, before, count) -> {
            aClass.setName(s.toString());
            txtName.setText(aClass.getName());
        });

        txtTeacher = view.findViewById(R.id.editTeacher_txtTeacher);
        txtDate = view.findViewById(R.id.editSchedule_txtDate);

        view.findViewById(R.id.editTeacher_changeTeacher).setOnClickListener(v ->
                activity.pushFragment(
                        PERSON_SELECT_FRAGMENT,
                        PersonSelectCtrl.PersonType.TEACHER,
                        aClass,
                        (PersonSelectCtrl.PersonSelectCallback) person -> {
                            aClass.setTeacher((Teacher) person);
                            parent.getDatabase().postCourse(aClass.getCourse());
                        }));
        View.OnClickListener dateEditClickListener = v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(aClass.getSchedule().getEnd());
            Util.newDatePicker(view, cal, aClass.getSchedule(), Util.SCHEDULE_START, true).show();
        };
        view.findViewById(R.id.editSchedule_changeDate).setOnClickListener(dateEditClickListener);
        txtDate.setOnClickListener(dateEditClickListener);
        view.findViewById(R.id.editSchedule_makeRecurring).setOnClickListener(v ->
                activity.pushFragment(
                        FragmentId.SCHEDULE_EDIT_FRAGMENT,
                        new ScheduleEditCtrl.ScheduleView() {
                            @Override
                            public Schedule getSchedule() {
                                return aClass.getSchedule();
                            }

                            @Override
                            public boolean postModel(DatabaseLink database) {
                                return ClassEditCtrl.this.postModel(database);
                            }
                        }));

        aClass.getSchedule().addChangeListener((ScheduleChangeListener) () -> this);
    }

    @Override
    public void duplicateModel() {
        aClass.getCourse().newClass(aClass.getName(), aClass.getSchedule());
    }

    @Override
    public void deleteModel() {
        aClass.getCourse().removeClass(aClass);
    }

    @Override
    public boolean postModel(DatabaseLink database) {
        return database.postCourse(aClass.getCourse());
    }

    @Override
    public void updateInfo() {
        txtSubject.setText(aClass.getCourse().getSubject().getName());
        txtCourse.setText(aClass.getCourse().getName());
        txtName.setText(txtName.getContext().getString(R.string.editClass_name, aClass.getName()));
        txtEditName.setText(aClass.getName());
        if (aClass.getTeacher() != null) {
            txtTeacher.setText(txtTeacher.getContext().getString(R.string.editModel_teacher,
                    aClass.getTeacher().getFullName()));
        } else {
            txtTeacher.setText(R.string.editModel_noTeacher);
        }
        txtDate.setText(Util.formatDate(aClass.getSchedule()));
    }

    public Class getTheClass() {
        return aClass;
    }

    public void setTheClass(UUID classId) {
        setTheClass((Class) parent.getCalendar().search(classId));
    }

    public void setTheClass(Class aClass) {
        this.aClass = aClass;
    }
}
