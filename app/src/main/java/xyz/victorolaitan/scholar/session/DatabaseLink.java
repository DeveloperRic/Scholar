package xyz.victorolaitan.scholar.session;

import java.util.UUID;

import xyz.victorolaitan.scholar.model.Club;
import xyz.victorolaitan.scholar.model.Event;
import xyz.victorolaitan.scholar.model.Person;
import xyz.victorolaitan.scholar.model.Student;
import xyz.victorolaitan.scholar.model.Teacher;
import xyz.victorolaitan.scholar.model.Todo;
import xyz.victorolaitan.scholar.model.subject.Course;
import xyz.victorolaitan.scholar.model.subject.Evaluation;
import xyz.victorolaitan.scholar.model.subject.Subject;

public interface DatabaseLink {

    Person getPerson(UUID id);

    Student getStudent(UUID id);

    Teacher getTeacher(UUID id);

    Club getClub(UUID id, Student owner);

    Event getEvent(UUID id);

    Todo getTodo(UUID id);

    Course getCourse(UUID id, Subject owner);

    Evaluation getEvaluation(UUID id, Course owner);

    boolean postPerson(Person person);

    boolean postClub(Club club);

    boolean postEvent(Event event);

    boolean postTodo(Todo todo);

    boolean postCourse(Course course);

    boolean postEvaluation(Evaluation evaluation);
}
