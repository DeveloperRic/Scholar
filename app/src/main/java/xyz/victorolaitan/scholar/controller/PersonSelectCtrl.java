package xyz.victorolaitan.scholar.controller;

import android.app.DatePickerDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.fragment.FragmentActivity;
import xyz.victorolaitan.scholar.fragment.FragmentId;
import xyz.victorolaitan.scholar.model.Club;
import xyz.victorolaitan.scholar.model.Person;
import xyz.victorolaitan.scholar.model.Student;
import xyz.victorolaitan.scholar.model.Teacher;
import xyz.victorolaitan.scholar.model.Todo;
import xyz.victorolaitan.scholar.model.subject.Class;
import xyz.victorolaitan.scholar.model.subject.Course;
import xyz.victorolaitan.scholar.model.subject.Subject;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.Indexable;
import xyz.victorolaitan.scholar.util.Nameable;
import xyz.victorolaitan.scholar.util.ScholarModel;
import xyz.victorolaitan.scholar.util.TextChangeListener;
import xyz.victorolaitan.scholar.util.Util;

public class PersonSelectCtrl implements FragmentCtrl {
    private Session session;
    private FragmentActivity activity;
    private PersonType type = PersonType.PERSON;
    private Indexable context;
    private Person person;
    private PersonSelectCallback callback;
    public List<PersonCard> observablePeople;

    private TextView txtDob;
    private RecyclerAdapter peopleAdapter;

    public PersonSelectCtrl(Session session, FragmentActivity activity) {
        this.session = session;
        this.activity = activity;
        observablePeople = new ArrayList<>();
    }

    public void init(View view) {
        if (type == PersonType.TEACHER) {
            person = new Teacher("", "", "", new Date());
        } else if (type == PersonType.STUDENT) {
            person = new Student("", "", "", new Date());
        } else {
            person = new Person("", "", "", new Date());
        }

        EditText editName = view.findViewById(R.id.selectPerson_editName);
        editName.addTextChangedListener((TextChangeListener)
                (s, start, before, count) -> person.parseFullName(s.toString()));

        txtDob = view.findViewById(R.id.selectPerson_txtDob);
        txtDob.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(person.getDob());
            new DatePickerDialog(
                    view.getContext(),
                    (view1, year, month, dayOfMonth) -> {
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        person.setDob(cal.getTime());
                        updateInfo();
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
        });
        view.findViewById(R.id.selectPerson_btnAdd).setOnClickListener(v -> {
            if (callback != null)
                callback.onSelect(person);
            activity.popFragment(FragmentId.PERSON_SELECT_FRAGMENT);
        });
    }

    @Override
    public void updateInfo() {
        txtDob.setText(Util.formatDate(person.getDob()));

        observablePeople.clear();
        updateFromStudent(session.getStudent());
        peopleAdapter.notifyDataSetChanged();
    }

    private void updateFromCourse(Course course) {
        for (Class aClass : course.getClasses()) {
            if (type != PersonType.STUDENT && notInList(aClass.getTeacher())) {
                observablePeople.add(new PersonCard(aClass.getTeacher(), aClass));
            }
        }
    }

    private void updateFromSubject(Subject subject) {
        for (Course course : subject.getCourseList()) {
            if (type != PersonType.STUDENT && notInList(course.getTeacher())) {
                observablePeople.add(new PersonCard(course.getTeacher(), course));
            }
            updateFromCourse(course);
        }
    }

    private void updateFromStudent(Student student) {
        for (Subject subject : student.getCalendar().getSubjects()) {
            updateFromSubject(subject);
        }
        for (Club club : student.getClubs()) {
            if (type != PersonType.TEACHER && notInList(club.getOwner())) {
                observablePeople.add(new PersonCard(club.getOwner(), club));
            }
        }
        //for (Todo todo : student.getCalendar().getTodos()) {
        //for (UUID uuid : to*do.getStudentDelegateIds()) {
        //TODO sync student objects
        //}
        //}
    }

    private boolean notInList(Indexable indexable) {
        for (PersonCard card : observablePeople) {
            if (card.p.getId().equals(indexable.getId()))
                return false;
        }
        return true;
    }

    public PersonType getPersonType() {
        return type;
    }

    public Indexable getContext() {
        return context;
    }

    public void setContext(PersonType type, Indexable context) {
        this.type = type;
        this.context = context;
    }

    public void setPeopleAdapter(RecyclerAdapter peopleAdaper) {
        this.peopleAdapter = peopleAdaper;
    }

    public void setCallback(PersonSelectCallback callback) {
        this.callback = callback;
    }

    public enum PersonType {
        PERSON, STUDENT, TEACHER
    }

    public final class PersonCard implements RecyclerCard, Comparable<PersonCard> {
        private Person p;
        private Indexable context;

        private TextView txtName;
        private TextView txtContext;

        private PersonCard(Person p, Indexable context) {
            this.p = p;
            this.context = context;
        }

        public String getName() {
            return p.getName();
        }

        @Override
        public void attachLayoutViews(View layout, CardView cv) {
            txtName = layout.findViewById(R.id.person_cardview_name);
            txtContext = layout.findViewById(R.id.person_cardview_context);
            cv.setOnClickListener(v -> {
                if (callback != null)
                    callback.onSelect(p);
                activity.popFragment(FragmentId.PERSON_SELECT_FRAGMENT);
            });
        }

        @Override
        public void updateInfo() {
            txtName.setText(p.getFullName());
            if (context instanceof Nameable) {
                txtContext.setText(((Nameable) context).getFancyName());
            }
        }

        @Override
        public int compareTo(PersonCard o) {
            if (getContextPriority(context) == getContextPriority(o.context)) {
                return p.compareTo(o);
            } else {
                return getContextPriority(context) < getContextPriority(o.context) ? -1 : 1;
            }
        }
    }

    private static int getContextPriority(ScholarModel model) {
        if (model instanceof Class) {
            return 0;
        }
        if (model instanceof Course) {
            return 1;
        }
        if (model instanceof Club) {
            return 2;
        }
        if (model instanceof Todo) {
            return 3;
        }
        return 999;
    }

    public interface PersonSelectCallback {
        void onSelect(Person person);
    }
}
