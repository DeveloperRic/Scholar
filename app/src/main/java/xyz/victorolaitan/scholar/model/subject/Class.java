package xyz.victorolaitan.scholar.model.subject;

import android.support.annotation.NonNull;

import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.model.Teacher;
import xyz.victorolaitan.scholar.util.HueHolder;
import xyz.victorolaitan.scholar.util.Indexable;
import xyz.victorolaitan.scholar.util.ScheduleHolder;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.SubjectHue;

public class Class implements Indexable, ScheduleHolder, HueHolder {

    UUID id = UUID.randomUUID();

    private Course course;
    private String code;
    private Schedule schedule;
    private Teacher teacher;

    Class(Course course) {
        this.course = course;
    }

    Class(Course course, String code, @NonNull Schedule schedule, Teacher teacher) {
        this.id = UUID.randomUUID();
        this.course = course;
        this.code = code;
        this.schedule = schedule;
        this.teacher = teacher;
    }

    @NonNull
    @Override
    public UUID getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    /**
     * @return this class' code
     */
    @Override
    public String getName() {
        return code;
    }

    @Override
    public void setName(String name) {
        code = name;
    }

    @Override
    public String getFancyName() {
        return course.getFancyName() + " " + getName();
    }

    @Override
    public String getShortName() {
        return "Class";
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + code + " - (" + teacher.consoleFormat("") + ") on " + schedule.consoleFormat("") + "\n";
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("id", id.toString());
        json.putPrimitive("code", code);
        json.putStructure("schedule", schedule.toJSON());
        json.putStructure("teacher", teacher.toJSON());
        return json.getRootNode();
    }

    @Override
    public Class fromJSON(JSONElement json) {
        id = UUID.fromString(json.valueOf("id"));
        code = json.valueOf("code");
        schedule = new Schedule().fromJSON(json.search("schedule"));
        teacher = new Teacher().fromJSON(json.search("teacher"));
        return this;
    }

    @Override
    public int getCompareType() {
        return COMPLEX;
    }

    @Override
    public java.lang.Comparable getCompareObject() {
        return schedule;
    }

    @Override
    public SubjectHue getHue() {
        return course.getHue();
    }
}
