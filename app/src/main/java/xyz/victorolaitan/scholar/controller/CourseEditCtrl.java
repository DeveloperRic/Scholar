package xyz.victorolaitan.scholar.controller;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.fragment.FragmentActivity;
import xyz.victorolaitan.scholar.model.Teacher;
import xyz.victorolaitan.scholar.model.subject.Class;
import xyz.victorolaitan.scholar.model.subject.Course;
import xyz.victorolaitan.scholar.session.DatabaseLink;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.TextChangeListener;

import static xyz.victorolaitan.scholar.fragment.FragmentId.CLASS_EDIT_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.EVALUATION_EDIT_FRAGMENT;
import static xyz.victorolaitan.scholar.fragment.FragmentId.PERSON_SELECT_FRAGMENT;

public class CourseEditCtrl implements ModelCtrl {
    private Session parent;
    private FragmentActivity activity;
    private Course course;

    public List<ClassCard> observableCards;

    private TextView txtSubject;
    private TextView txtName;
    private TextView txtTeacher;
    private EditText txtEditName;
    private EditText txtEditCode;
    private RecyclerAdapter classesAdapter;

    public CourseEditCtrl(Session parent, FragmentActivity activity) {
        this.parent = parent;
        this.activity = activity;
        observableCards = new ArrayList<>();
    }

    public void init(View view) {
        txtSubject = view.findViewById(R.id.editCourse_txtSubject);
        txtName = view.findViewById(R.id.editCourse_txtName);

        txtEditName = view.findViewById(R.id.editCourse_editName);
        txtEditName.addTextChangedListener((TextChangeListener) (s, start, before, count) -> {
            course.setName(s.toString());
            txtName.setText(course.getName());
        });

        txtEditCode = view.findViewById(R.id.editCourse_editCode);
        txtEditCode.addTextChangedListener(
                (TextChangeListener) (s, start, before, count) -> course.setCode(s.toString()));

        txtTeacher = view.findViewById(R.id.editTeacher_txtTeacher);

        view.findViewById(R.id.editTeacher_changeTeacher).setOnClickListener(v ->
                activity.pushFragment(
                        PERSON_SELECT_FRAGMENT,
                        PersonSelectCtrl.PersonType.TEACHER,
                        course,
                        (PersonSelectCtrl.PersonSelectCallback) person -> {
                            course.setTeacher((Teacher) person);
                            parent.getDatabase().postCourse(course);
                        }));
        view.findViewById(R.id.editCourse_btnEvaluation).setOnClickListener(v ->
                activity.pushFragment(EVALUATION_EDIT_FRAGMENT, course.getEvaluation()));
        view.findViewById(R.id.editCourse_addClass).setOnClickListener(v ->
                activity.pushFragment(CLASS_EDIT_FRAGMENT, course.newClass("", new Schedule())));
    }

    @Override
    public void duplicateModel() {
        course.getSubject().newCourse(course.getName(), course.getCode(), course.getTeacher());
    }

    @Override
    public void deleteModel() {
        course.getSubject().removeCourse(course.getId());
    }

    @Override
    public boolean postModel(DatabaseLink database) {
        return database.postCourse(course);
    }

    @Override
    public void updateInfo() {
        txtSubject.setText(course.getSubject().getName());
        txtName.setText(course.getName());
        txtEditName.setText(course.getName());
        txtEditCode.setText(course.getCode());
        if (course.getTeacher() != null) {
            txtTeacher.setText(txtTeacher.getContext().getString(R.string.editModel_teacher,
                    course.getTeacher().getFullName()));
        } else {
            txtTeacher.setText(R.string.editModel_noTeacher);
        }
    }

    public void refreshCards() {
        observableCards.clear();
        for (Class aClass : course.getClasses()) {
            observableCards.add(new ClassCard(aClass));
        }
        classesAdapter.notifyDataSetChanged();
    }

    public void setClassesAdapter(RecyclerAdapter classesAdapter) {
        this.classesAdapter = classesAdapter;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(UUID courseId) {
        setCourse((Course) parent.getCalendar().search(courseId));
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public final class ClassCard implements RecyclerCard {
        private Class aClass;

        private TextView txtClassCode;
        private TextView txtClassTime;

        private ClassCard(Class aClass) {
            this.aClass = aClass;
        }

        public String getName() {
            return aClass.getName();
        }

        @Override
        public void attachLayoutViews(View layout, CardView cv) {
            txtClassCode = layout.findViewById(R.id.classes_cardview_code);
            txtClassTime = layout.findViewById(R.id.classes_cardview_time);
            cv.setOnClickListener(v ->
                    activity.pushFragment(CLASS_EDIT_FRAGMENT, aClass));
        }

        @Override
        public void updateInfo() {
            txtClassCode.setText(aClass.getName());
            txtClassTime.setText(aClass.getSchedule().consoleFormat(""));
        }
    }
}
