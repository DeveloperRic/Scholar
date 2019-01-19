package xyz.victorolaitan.scholar.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.model.subject.Subject;
import xyz.victorolaitan.scholar.util.Filterable;
import xyz.victorolaitan.scholar.util.Nameable;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.ScholarModel;
import xyz.victorolaitan.scholar.util.Searchable;

public class Calendar implements Filterable, Searchable<ScholarModel> {

    private List<Subject> subjects = new ArrayList<>();
    private List<Event> events = new ArrayList<>();
    private List<Todo> todos = new ArrayList<>();

    public List<Subject> getSubjects() {
        return subjects;
    }

    public Subject newSubject(String name, String code) {
        Subject subject = Subject.newSubject(name, code);
        subjects.add(subject);
        return subject;
    }

    public void removeSubject(Subject subject) {
        subjects.remove(subject);
    }

    public List<Event> getEvents() {
        return events;
    }

    public Event newEvent(String title, String description, Schedule date) {
        Event event = new Event(title, description, date);
        events.add(event);
        return event;
    }

    public void removeEvent(Event event) {
        events.remove(event);
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public Todo newTodo(String title, String description) {
        Todo todo = new Todo(title, description);
        todos.add(todo);
        return todo;
    }

    public void removeTodo(Todo todo) {
        todos.remove(todo);
    }

    @Override
    public List<Nameable> filter(Date date) {
        List<Nameable> list = new ArrayList<>();
        for (Subject subject : subjects) {
            if (!subject.filter(date).isEmpty())
                list.add(subject);
        }
        return list;
    }

    @Override
    public List<Nameable> filterRecursively(Date date) {
        List<Nameable> list = new ArrayList<>();
        for (Subject subject : subjects) {
            list.addAll(subject.filterRecursively(date));
        }
        for (Event event : events) {
            if (event.getDate().occursOn(date))
                list.add(event);
        }
        for (Todo todo : todos) {
            if (todo.getDeadline() == null)
                continue;

            if (todo.getDeadline().occursOn(date)) {
                list.add(todo);
            } else {
                for (Schedule schedule : todo.getReminders()) {
                    if (schedule.occursOn(date)) {
                        list.add(todo);
                        break;
                    }
                }
            }
        }
        return list;
    }

    @Override
    public String consoleFormat(String prefix) {
        StringBuilder sb = new StringBuilder(prefix + "Subjects:\n");
        for (Subject subject : subjects)
            sb.append(subject.consoleFormat(prefix + " "));

        sb.append("\nEvents:\n");
        for (Event event : events)
            sb.append(event.consoleFormat(prefix + " "));

        sb.append("\nTodos\n");
        for (Todo todo : todos)
            sb.append(todo.consoleFormat(prefix + " "));

        return sb.append("\n").toString();
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putArray("subjects");
        for (Subject subject : subjects) {
            json.search("subjects").putElement(subject.toJSON());
        }
        json.putArray("events");
        for (Event event : events) {
            json.search("events").putElement(event.toJSON());
        }
        json.putArray("todos");
        for (Todo todo : todos) {
            json.search("todos").putElement(todo.toJSON());
        }
        return json.getRootNode();
    }

    @Override
    public Calendar fromJSON(JSONElement json) {
        subjects.clear();
        for (JSONElement e : json.search("subjects").getChildren()) {
            newSubject(null, null).fromJSON(e);
        }
        events.clear();
        for (JSONElement e : json.search("events").getChildren()) {
            events.add(new Event().fromJSON(e));
        }
        todos.clear();
        for (JSONElement e : json.search("todos").getChildren()) {
            todos.add(new Todo().fromJSON(e));
        }
        return this;
    }

    @Override
    public ScholarModel search(UUID query) {
        for (Subject subject : subjects) {
            if (subject.getSubjectId().equals(query)) {
                return subject;
            }
        }
        for (Subject subject : subjects) {
            ScholarModel deepSearch = subject.search(query);
            if (deepSearch != null)
                return deepSearch;
        }
        return null;
    }
}
