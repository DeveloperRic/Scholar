package xyz.victorolaitan.scholar.model.subject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.model.Teacher;
import xyz.victorolaitan.scholar.util.Filterable;
import xyz.victorolaitan.scholar.util.Nameable;
import xyz.victorolaitan.scholar.util.ScholarModel;
import xyz.victorolaitan.scholar.util.Searchable;

public class Subject implements Nameable, Filterable, Searchable<ScholarModel> {

    private UUID subjectId;
    private String name;
    private String code;

    private List<Course> courseList;

    public static Subject newSubject(String name, String code) {
        return new Subject(name, code);
    }

    private Subject(String name, String code, Course... courses) {
        this.subjectId = UUID.randomUUID();
        this.name = name;
        this.code = code;
        this.courseList = new ArrayList<>();
        this.courseList.addAll(Arrays.asList(courses));
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @return subject code
     */
    public String getFancyName() {
        return code;
    }

    /**
     *
     * @return "Subject"
     */
    @Override
    public String getShortName() {
        return "Subject";
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

    public List<Course> getCourseList() {
        return courseList;
    }

    public Course newCourse(String name, String code, Teacher teacher) {
        Course course = new Course(this, name, code, teacher);
        courseList.add(course);
        return course;
    }

    public void removeCourse(UUID courseId) {
        for (Course course : courseList) {
            if (course.id.equals(courseId)) {
                courseList.remove(course);
                break;
            }
        }
    }

    public List<Nameable> filter(Date date) {
        List<Nameable> list = new ArrayList<>();
        for (Course course : courseList) {
            if (!course.filter(date).isEmpty())
                list.add(course);
        }
        return list;
    }

    @Override
    public List<Nameable> filterRecursively(Date date) {
        List<Nameable> list = new ArrayList<>();
        for (Course course : courseList) {
            list.addAll(course.filterRecursively(date));
        }
        return list;
    }

    @Override
    public String consoleFormat(String prefix) {
        StringBuilder sb = new StringBuilder(prefix + "Subject: " + name + " (" + code + "):\n");

        for (Course course : courseList)
            sb.append(prefix).append(course.consoleFormat(prefix + "  "));

        return sb.append("\n").toString();
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("subjectId", subjectId.toString());
        json.putPrimitive("name", name);
        json.putPrimitive("code", code);
        json.putArray("courseList");
        for (Course course : courseList) {
            json.search("courseList").putElement(course.toJSON());
        }
        return json.getRootNode();
    }

    @Override
    public Subject fromJSON(JSONElement json) {
        subjectId = UUID.fromString(json.valueOf("subjectId"));
        name = json.valueOf("name");
        code = json.valueOf("code");
        courseList.clear();
        for (JSONElement e : json.search("courseList").getChildren()) {
            courseList.add(new Course(this).fromJSON(e));
        }
        return this;
    }

    @Override
    public ScholarModel search(UUID query) {
        for (Course course : courseList) {
            if (course.id.equals(query)) {
                return course;
            }
        }
        for (Course course : courseList) {
            ScholarModel deepSearch = course.search(query);
            if (deepSearch != null)
                return deepSearch;
        }
        return null;
    }
}
