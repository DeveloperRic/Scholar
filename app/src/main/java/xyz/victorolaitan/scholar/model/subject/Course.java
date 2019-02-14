package xyz.victorolaitan.scholar.model.subject;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.model.Teacher;
import xyz.victorolaitan.scholar.session.DatabaseLink;
import xyz.victorolaitan.scholar.util.Filterable;
import xyz.victorolaitan.scholar.util.HueHolder;
import xyz.victorolaitan.scholar.util.Indexable;
import xyz.victorolaitan.scholar.util.ModelLink;
import xyz.victorolaitan.scholar.util.Nameable;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.ScholarModel;
import xyz.victorolaitan.scholar.util.Searchable;
import xyz.victorolaitan.scholar.util.SubjectHue;

public class Course implements Nameable, Filterable, Indexable, Searchable<ScholarModel>,HueHolder {

    UUID id;

    private Subject subject;
    private String name;
    private String code;
    private Teacher teacher;
    private ModelLink<Evaluation> evaluation;

    private List<Class> classes;

    public Course(Subject subject) {
        this.subject = subject;
        classes = new ArrayList<>();
        evaluation = new ModelLink<Evaluation>(new Evaluation(this)) {
            @Override
            protected Evaluation getMethod(DatabaseLink link, UUID id) {
                return link.getEvaluation(id, Course.this);
            }

            @Override
            protected boolean postMethod(DatabaseLink link, Evaluation model) {
                return link.postEvaluation(model);
            }
        };
    }

    Course(Subject subject, String name, String code, Teacher teacher) {
        this.id = UUID.randomUUID();
        this.subject = subject;
        this.name = name;
        this.code = code;
        this.teacher = teacher;
        evaluation = new ModelLink<Evaluation>(new Evaluation(this)) {
            @Override
            protected Evaluation getMethod(DatabaseLink link, UUID id) {
                return link.getEvaluation(id, Course.this);
            }

            @Override
            protected boolean postMethod(DatabaseLink link, Evaluation model) {
                return link.postEvaluation(model);
            }
        };
        classes = new ArrayList<>();
    }

    @NonNull
    @Override
    public UUID getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getFancyName() {
        return subject.getFancyName() + " " + getCode();
    }

    @Override
    public String getShortName() {
        return "Course";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Evaluation getEvaluation() {
        return evaluation.get();
    }

    public List<Class> getClasses() {
        return classes;
    }

    public Class newClass(String code, Schedule schedule) {
        Class aClass = new Class(this, code, schedule, teacher);
        classes.add(aClass);
        return aClass;
    }

    public void removeClass(Class aClass) {
        classes.remove(aClass);
    }

    public List<Nameable> filter(Date date) {
        List<Nameable> list = new ArrayList<>();
        for (Class aClass : classes) {
            if (aClass.getSchedule().occursOn(date))
                list.add(aClass);
        }
        return list;
    }

    @Override
    public List<Nameable> filterRecursively(Date date) {
        List<Nameable> list = new ArrayList<>(filter(date));
        list.addAll(evaluation.get().filter(date));
        return list;
    }

    @Override
    public String consoleFormat(String prefix) {
        StringBuilder sb = new StringBuilder(prefix + name + " " + code + " (" + teacher.consoleFormat("") + ")\n");

        sb.append(prefix).append("Classes:\n");
        for (Class aClass : classes)
            sb.append(prefix).append(aClass.consoleFormat(prefix + "  "));

        sb.append(evaluation.get().consoleFormat(prefix + "  "));

        return sb.append("\n").toString();
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("id", id.toString());
        json.putPrimitive("name", name);
        json.putPrimitive("code", code);
        json.putStructure("teacher", teacher.toJSON());
        json.putPrimitive("evaluation", evaluation.id().toString());
        json.putArray("classes");
        for (Class aClass : classes) {
            json.search("classes").putElement(aClass.toJSON());
        }
        return json.getRootNode();
    }

    @Override
    public Course fromJSON(JSONElement json) {
        id = UUID.fromString(json.valueOf("id"));
        name = json.valueOf("name");
        code = json.valueOf("code");
        teacher = new Teacher().fromJSON(json.search("teacher"));
        classes.clear();
        for (JSONElement e : json.search("classes").getChildren()) {
            classes.add(new Class(this).fromJSON(e));
        }
        return this;
    }

    @Override
    public ScholarModel search(UUID query) {
        for (Class aClass : classes) {
            if (aClass.id.equals(query)) {
                return aClass;
            }
        }
        return evaluation.get().search(query);
    }

    @Override
    public SubjectHue getHue() {
        return subject.getHue();
    }
}
