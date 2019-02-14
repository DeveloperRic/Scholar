package xyz.victorolaitan.scholar.controller;

import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.fragment.FragmentActivity;
import xyz.victorolaitan.scholar.model.Teacher;
import xyz.victorolaitan.scholar.model.subject.Course;
import xyz.victorolaitan.scholar.model.subject.Subject;
import xyz.victorolaitan.scholar.util.SubjectHue;
import xyz.victorolaitan.scholar.session.DatabaseLink;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.TextChangeListener;

import static xyz.victorolaitan.scholar.fragment.FragmentId.COURSE_EDIT_FRAGMENT;

public class SubjectEditCtrl implements ModelCtrl {
    private Session parent;
    private FragmentActivity activity;
    private Subject subject;

    public List<CourseCard> observableCards;

    private View parentView;
    private TextView txtName;
    private EditText txtEditName;
    private EditText txtEditCode;
    private RecyclerAdapter coursesAdapter;

    public SubjectEditCtrl(Session parent, FragmentActivity activity) {
        this.parent = parent;
        this.activity = activity;
        observableCards = new ArrayList<>();
    }

    public void init(View view) {
        this.parentView = view;
        txtName = view.findViewById(R.id.editSubject_txtName);

        txtEditName = view.findViewById(R.id.editSubject_editName);
        txtEditName.addTextChangedListener((TextChangeListener) (s, start, before, count) -> {
            subject.setName(s.toString());
            txtName.setText(subject.getName());
        });

        txtEditCode = view.findViewById(R.id.editSubject_editCode);
        txtEditCode.addTextChangedListener(
                (TextChangeListener) (s, start, before, count) -> subject.setCode(s.toString()));

        TextView btnAddCourse = view.findViewById(R.id.editSubject_addCourse);
        btnAddCourse.setOnClickListener(v ->
                activity.pushFragment(COURSE_EDIT_FRAGMENT, subject.newCourse(
                        "Some Course", "COURSE",
                        new Teacher("Teacher", "Name", "teacher@school.edu", new Date()))));

        setHueViewListeners(getHueViews());
    }

    @Override
    public void duplicateModel() {
        parent.getCalendar().newSubject(subject.getName(), subject.getCode());
    }

    @Override
    public void deleteModel() {
        parent.getCalendar().removeSubject(subject);
    }

    @Override
    public boolean postModel(DatabaseLink database) {
        return database.postPerson(parent.getStudent());
    }

    @Override
    public void updateInfo() {
        txtName.setText(subject.getName());
        txtEditName.setText(subject.getName());
        txtEditCode.setText(subject.getCode());
        updateHueViews(getHueViews());
    }

    public void refreshCards() {
        observableCards.clear();
        for (Course course : subject.getCourseList()) {
            observableCards.add(new CourseCard(course));
        }
        coursesAdapter.notifyDataSetChanged();
    }

    public void setCoursesAdapter(RecyclerAdapter coursesAdapter) {
        this.coursesAdapter = coursesAdapter;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(UUID uuid) {
        setSubject((Subject) parent.getCalendar().search(uuid));
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    private Object[][] getHueViews() {
        return new Object[][]{
                {parentView.findViewById(R.id.editSubject_huePink), SubjectHue.PINK},
                {parentView.findViewById(R.id.editSubject_huePurple), SubjectHue.PURPLE},
                {parentView.findViewById(R.id.editSubject_hueBlue), SubjectHue.BLUE},
                {parentView.findViewById(R.id.editSubject_hueTeal), SubjectHue.TEAL},
                {parentView.findViewById(R.id.editSubject_hueOrange), SubjectHue.ORANGE},
                {parentView.findViewById(R.id.editSubject_hueBrown), SubjectHue.BROWN},
                {parentView.findViewById(R.id.editSubject_hueGrey), SubjectHue.GREY}
        };
    }

    private void setHueViewListeners(Object[][] views) {
        for (Object[] view : views) {
            ((ImageView) view[0]).setOnClickListener(v -> {
                subject.setHue((SubjectHue) view[1]);
                updateHueViews(views);
            });
        }
    }

    private void updateHueViews(Object[][] views) {
        for (Object[] data : views) {
            ImageView view = (ImageView) data[0];
            if (data[1] == subject.getHue()) {
                view.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_check_white_24dp));
            } else {
                view.setImageDrawable(null);
            }
        }
    }

    public final class CourseCard implements RecyclerCard {
        private Course course;

        private TextView txtCourseName;

        private CourseCard(Course course) {
            this.course = course;
        }

        public String getName() {
            return course.getName();
        }

        @Override
        public void attachLayoutViews(View layout, CardView cv) {
            cv.setOnClickListener(v ->
                    activity.pushFragment(COURSE_EDIT_FRAGMENT, course));
            layout.findViewById(R.id.subjects_cardview_expand).setVisibility(View.GONE);
            txtCourseName = layout.findViewById(R.id.subjects_cardview_name);
            txtCourseName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        }

        @Override
        public void updateInfo() {
            txtCourseName.setText(course.getName());
        }
    }
}
