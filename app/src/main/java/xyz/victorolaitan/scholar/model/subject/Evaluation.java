package xyz.victorolaitan.scholar.model.subject;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.util.Filterable;
import xyz.victorolaitan.scholar.util.HueHolder;
import xyz.victorolaitan.scholar.util.Indexable;
import xyz.victorolaitan.scholar.util.Nameable;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.Searchable;
import xyz.victorolaitan.scholar.util.SubjectHue;

public class Evaluation implements Filterable, Searchable<EvalComponent>, Indexable, HueHolder {
    private Course course;
    private UUID id = UUID.randomUUID();

    private List<Deliverable> deliverables;
    private List<Test> tests;

    public Evaluation(Course course) {
        this.course = course;
        deliverables = new ArrayList<>();
        tests = new ArrayList<>();
    }

    public Course getCourse() {
        return course;
    }

    public List<Deliverable> getDeliverables() {
        return deliverables;
    }

    public Deliverable newDeliverable(String title, String description, Schedule deadline) {
        Deliverable deliverable = new Deliverable(this, title, description, deadline);
        deliverables.add(deliverable);
        return deliverable;
    }

    public void removeDeliverable(Deliverable deliverable) {
        deliverables.remove(deliverable);
    }

    public List<Test> getTests() {
        return tests;
    }

    public Test newTest(String title, String description, Schedule date) {
        Test test = new Test(this, title, description, date);
        tests.add(test);
        return test;
    }

    public void removeTest(Test test) {
        tests.remove(test);
    }

    @Override
    public List<Nameable> filter(Date date) {
        List<Nameable> list = new ArrayList<>();
        for (Deliverable deliverable : deliverables) {
            if (deliverable.getSchedule().occursOn(date))
                list.add(deliverable);
        }
        for (Test test : tests) {
            if (test.getSchedule().occursOn(date))
                list.add(test);
        }
        return list;
    }

    @Override
    public List<Nameable> filterRecursively(Date date) {
        return new ArrayList<>(filter(date));
    }

    @Override
    public String consoleFormat(String prefix) {
        StringBuilder sb = new StringBuilder();

        sb.append(prefix).append("Deliverables:\n");
        for (Deliverable deliverable : deliverables)
            sb.append(deliverable.consoleFormat(prefix + "  "));

        sb.append(prefix).append("Tests:\n");
        for (Test test : tests)
            sb.append(test.consoleFormat(prefix + "  "));

        return sb.append("\n").toString();
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("id", id.toString());
        json.putArray("deliverables");
        for (Deliverable deliverable : deliverables) {
            json.search("deliverables").putElement(deliverable.toJSON());
        }
        json.putArray("tests");
        for (Test test : tests) {
            json.search("tests").putElement(test.toJSON());
        }
        return json.getRootNode();
    }

    @Override
    public Evaluation fromJSON(JSONElement json) {
        id = UUID.fromString(json.valueOf("id"));
        deliverables.clear();
        for (JSONElement e : json.search("deliverables").getChildren()) {
            deliverables.add(new Deliverable(this).fromJSON(e));
        }
        tests.clear();
        for (JSONElement e : json.search("tests").getChildren()) {
            tests.add(new Test(this).fromJSON(e));
        }
        return this;
    }

    @Override
    public EvalComponent search(UUID query) {
        for (Deliverable deliverable : deliverables) {
            if (deliverable.getDeliverableId().equals(query)) {
                return deliverable;
            }
        }
        for (Test test : tests) {
            if (test.getTestId().equals(query)) {
                return test;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public SubjectHue getHue() {
        return course.getHue();
    }
}
