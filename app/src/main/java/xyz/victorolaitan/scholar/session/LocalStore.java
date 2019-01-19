package xyz.victorolaitan.scholar.session;

import android.content.Context;

import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.EasyJSONException;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.easyjson.SafeJSONElementType;
import xyz.victorolaitan.scholar.model.Club;
import xyz.victorolaitan.scholar.model.Event;
import xyz.victorolaitan.scholar.model.Person;
import xyz.victorolaitan.scholar.model.Student;
import xyz.victorolaitan.scholar.model.Teacher;
import xyz.victorolaitan.scholar.model.Todo;
import xyz.victorolaitan.scholar.model.subject.Course;
import xyz.victorolaitan.scholar.model.subject.Evaluation;
import xyz.victorolaitan.scholar.model.subject.Subject;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.ScholarModel;

final class LocalStore implements DatabaseLink {
    private static final String PEOPLE_FILE = "people.txt";
    private static final String CLUBS_FILE = "clubs.txt";
    private static final String EVENTS_FILE = "events.txt";
    private static final String TODOS_FILE = "todos.txt";
    private static final String COURSES_FILE = "courses.txt";
    private static final String EVALUATIONS_FILE = "evaluations.txt";

    private Context context;

    LocalStore(Context context) {
        this.context = context;
    }

    @Override
    public Person getPerson(UUID id) {
        return new Person().fromJSON(searchLocal(PEOPLE_FILE, id));
    }

    @Override
    public Student getStudent(UUID id) {
        return new Student().fromJSON(searchLocal(PEOPLE_FILE, id));
    }

    @Override
    public Teacher getTeacher(UUID id) {
        return new Teacher().fromJSON(searchLocal(PEOPLE_FILE, id));
    }

    @Override
    public Club getClub(UUID id, Student owner) {
        return new Club(owner).fromJSON(searchLocal(CLUBS_FILE, id));
    }

    @Override
    public Event getEvent(UUID id) {
        return new Event().fromJSON(searchLocal(EVENTS_FILE, id));
    }

    @Override
    public Todo getTodo(UUID id) {
        return new Todo().fromJSON(searchLocal(TODOS_FILE, id));
    }

    @Override
    public Course getCourse(UUID id, Subject owner) {
        return new Course(owner).fromJSON(searchLocal(COURSES_FILE, id));
    }

    @Override
    public Evaluation getEvaluation(UUID id, Course owner) {
        return new Evaluation(owner).fromJSON(searchLocal(EVALUATIONS_FILE, id));
    }

    @Override
    public boolean postPerson(Person person) {
        return saveLocal(PEOPLE_FILE, person);
    }

    @Override
    public boolean postClub(Club club) {
        return saveLocal(CLUBS_FILE, club);
    }

    @Override
    public boolean postEvent(Event event) {
        return saveLocal(EVENTS_FILE, event);
    }

    @Override
    public boolean postTodo(Todo todo) {
        return saveLocal(TODOS_FILE, todo);
    }

    @Override
    public boolean postCourse(Course course) {
        return saveLocal(COURSES_FILE, course);
    }

    @Override
    public boolean postEvaluation(Evaluation evaluation) {
        return saveLocal(EVALUATIONS_FILE, evaluation);
    }

    private JSONElement searchLocal(String fileName, UUID id) {
        return Objects.requireNonNull(retrieveLocal(fileName)).search(id.toString());
    }

    private boolean saveLocal(String filename, ScholarModel model) {
        EasyJSON json = retrieveLocal(filename);
        Objects.requireNonNull(json).putElement(model.toJSON());
        try {
            json.save();
        } catch (EasyJSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private EasyJSON retrieveLocal(String fileName) {
        File file = getLocalFile(fileName);
        if (file.exists()) {
            try {
                return EasyJSON.open(file);
            } catch (EasyJSONException e) {
                e.printStackTrace();
            }
        } else {
            populateStores();
            retrieveLocal(fileName);
        }
        return null;
    }

    private File getLocalFile(String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    private void populateStores() {
        Student student = new Student("A", "Scholar", "scholar@school.edu", new Date());
        Student dude = new Student("Another", "Straight-shooter", "topshot@school.edu", new Date());
        Teacher mark = new Teacher("Mark", "Lanthier", "mark.lanthier@school.edu", new Date());
        Teacher christine = new Teacher("Christine", "Laurendeau", "christine.laurendeau@school.edu", new Date());
        Teacher eaket = new Teacher("Christopher", "Eaket", "christine.laurendeau@school.edu", new Date());

        Subject comp = student.getCalendar().newSubject("Computer Science", "COMP");
        Subject digh = student.getCalendar().newSubject("Digital Humanities", "DIGH");

        populateNewStore(PEOPLE_FILE, student, dude, mark, christine, eaket);

        Course java = comp.newCourse("Intro to Java", "1406", mark);
        java.newClass("B", new Schedule());

        Course cplus = comp.newCourse("Intro to Software Engineering", "2404", christine);
        cplus.newClass("A", new Schedule());

        Course intro = digh.newCourse("Intro to Digital Hums", "2001", eaket);

        populateNewStore(COURSES_FILE, java, cplus, intro);

        java.getEvaluation().newDeliverable("Assignment 2", "", new Schedule());
        java.getEvaluation().newTest("Midterm", "Topics 1-6", new Schedule());
        java.getEvaluation().newTest("Final", "Sucks to be u", new Schedule());

        cplus.getEvaluation().newDeliverable("Assignment 1", "Bullsh$t", new Schedule());
        cplus.getEvaluation().newDeliverable("Assignment 2", "", new Schedule());
        cplus.getEvaluation().newTest("Midterm", "Topics 1-4", new Schedule());
        cplus.getEvaluation().newTest("Final", "Sucks to be u", new Schedule());

        intro.newClass("A", new Schedule());
        intro.getEvaluation().newDeliverable("Project analysis", "", new Schedule());
        intro.getEvaluation().newDeliverable("Response paper", "", new Schedule());
        intro.getEvaluation().newDeliverable("Design doc", "", new Schedule());
        intro.getEvaluation().newDeliverable("Final paper", "", new Schedule());

        populateNewStore(EVALUATIONS_FILE, java.getEvaluation(), cplus.getEvaluation(), intro.getEvaluation());

        populateNewStore(EVENTS_FILE,
                student.getCalendar().newEvent("Rate my prof day", "", new Schedule()));
        populateNewStore(TODOS_FILE,
                student.getCalendar().newTodo("Contest A1 marks", "TA:paul"));

        Club club = student.newClub("Retro Music Club", "Synth pop lovers welcome too :)");
        club.addMember(dude);
        populateNewStore(CLUBS_FILE, club);
    }

    private void populateNewStore(String fileName, ScholarModel... models) {
        EasyJSON store = EasyJSON.create(getLocalFile(PEOPLE_FILE));
        store.getRootNode().setType(SafeJSONElementType.ARRAY);
        for (ScholarModel model : models) {
            store.putElement(model.toJSON());
        }
        try {
            store.save();
        } catch (EasyJSONException e) {
            e.printStackTrace();
        }
    }
}
