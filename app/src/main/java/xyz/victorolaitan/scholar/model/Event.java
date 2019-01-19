package xyz.victorolaitan.scholar.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.util.Location;
import xyz.victorolaitan.scholar.util.Observable;
import xyz.victorolaitan.scholar.util.Schedule;

public class Event implements Observable {

    private String title;
    private String description;
    private Schedule date;
    private Location location;
    private List<Schedule> reminders;

    public Event() {
        reminders = new ArrayList<>();
    }

    Event(String title, String description, @NonNull Schedule date) {
        this.title = title;
        this.description = description;
        this.date = date;
        reminders = new ArrayList<>();
    }

    @Override
    public String getFancyName() {
        return getName();
    }

    @Override
    public String getShortName() {
        return "Event";
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

    public Schedule getDate() {
        return date;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Schedule> getReminders() {
        return reminders;
    }

    public void addReminders(Schedule... reminders) {
        this.reminders.addAll(Arrays.asList(reminders));
    }

    public void removeReminder(Schedule reminder) {
        reminders.remove(reminder);
    }

    @Override
    public String consoleFormat(String prefix) {
        return "";
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("title", title);
        json.putPrimitive("description", description);
        json.putStructure("date", date.toJSON());
        if (location != null) {
            json.putStructure("location", location.toJSON());
        }
        json.putArray("reminders");
        for (Schedule reminder : reminders) {
            json.search("reminder").putElement(reminder.toJSON());
        }
        return json.getRootNode();
    }

    @Override
    public Event fromJSON(JSONElement json) {
        title = json.valueOf("title");
        description = json.valueOf("description");
        date = new Schedule().fromJSON(json.search("date"));
        if (json.elementExists("location")) {
            location = new Location().fromJSON(json.search("location"));
        }
        reminders.clear();
        for (JSONElement e : json.search("reminders").getChildren()) {
            reminders.add(new Schedule().fromJSON(e));
        }
        return this;
    }

    @Override
    public int getCompareType() {
        return COMPLEX;
    }

    @Override
    public java.lang.Comparable getCompareObject() {
        return date;
    }
}
