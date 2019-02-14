package xyz.victorolaitan.scholar.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.util.Indexable;
import xyz.victorolaitan.scholar.util.Observable;
import xyz.victorolaitan.scholar.util.Schedule;

public class Todo implements Observable, Indexable {

    private UUID id = UUID.randomUUID();

    private UUID parentTodoId;
    private String title;
    private String description;
    private Schedule deadline;

    private List<Schedule> reminders;
    private List<UUID> studentDelegateIds;

    public Todo() {
        reminders = new ArrayList<>();
        studentDelegateIds = new ArrayList<>();
    }

    Todo(String title, String description) {
        this.title = title;
        this.description = description;
        reminders = new ArrayList<>();
        studentDelegateIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public UUID getId() {
        return id;
    }

    public UUID getParentTodoId() {
        return parentTodoId;
    }

    public void setParentTodoId(UUID parentTodoId) {
        this.parentTodoId = parentTodoId;
    }

    @Override
    public String getFancyName() {
        return getName();
    }

    @Override
    public String getShortName() {
        return "Todo";
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Schedule getDeadline() {
        return deadline;
    }

    public void setDeadline(Schedule deadline) {
        this.deadline = deadline;
    }

    public List<Schedule> getReminders() {
        return reminders;
    }

    public void addReminders(Schedule... reminders) {
        if (this.reminders == null) {
            this.reminders = new ArrayList<>();
        }
        this.reminders.addAll(Arrays.asList(reminders));
    }

    public void removeReminder(Schedule reminder) {
        reminders.remove(reminder);
    }

    public List<UUID> getStudentDelegateIds() {
        return studentDelegateIds;
    }

    public void addStudentDelegateIds(UUID... studentDelegateIds) {
        if (this.studentDelegateIds == null) {
            this.studentDelegateIds = new ArrayList<>();
        }
        this.studentDelegateIds.addAll(Arrays.asList(studentDelegateIds));
    }

    public void removeStudentDelegateId(UUID studentDelegateId) {
        studentDelegateIds.remove(studentDelegateId);
    }

    @Override
    public String consoleFormat(String prefix) {
        return "";
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("todoId", id.toString());
        if (parentTodoId != null) {
            json.putPrimitive("parentTodoId", parentTodoId);
        }
        json.putPrimitive("title", title);
        json.putPrimitive("description", description);
        if (deadline != null) {
            json.putStructure("deadline", deadline.toJSON());
        }
        json.putArray("reminders");
        for (Schedule reminder : reminders) {
            json.search("reminders").putElement(reminder.toJSON());
        }
        json.putArray("studentDelegateIds");
        for (UUID delegate : studentDelegateIds) {
            json.search("studentDelegateIds").putPrimitive(delegate.toString());
        }
        return json.getRootNode();
    }

    @Override
    public Todo fromJSON(JSONElement json) {
        id = UUID.fromString(json.<String>valueOf("todoId"));
        if (json.elementExists("parentTodoId")) {
            parentTodoId = UUID.fromString(json.<String>valueOf("parentTodoId"));
        }
        title = json.valueOf("title");
        description = json.valueOf("description");
        if (json.elementExists("deadline")) {
            deadline = new Schedule().fromJSON(json.search("deadline"));
        }
        reminders.clear();
        for (JSONElement e : json.search("reminders").getChildren()) {
            reminders.add(new Schedule().fromJSON(e));
        }
        studentDelegateIds.clear();
        for (JSONElement e : json.search("studentDelegateIds").getChildren()) {
            studentDelegateIds.add(UUID.fromString(e.<String>getValue()));
        }
        return this;
    }

    @Override
    public int getCompareType() {
        return deadline != null ? COMPLEX : STRING;
    }

    @Override
    public java.lang.Comparable getCompareObject() {
        return deadline != null ? deadline : title;
    }
}
