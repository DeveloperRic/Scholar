package xyz.victorolaitan.scholar.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xyz.victorolaitan.easyjson.JSONElement;

public class Student extends Person {
    private Calendar calendar;
    private List<Club> clubs;

    public Student() {
        calendar = new Calendar();
        clubs = new ArrayList<>();
    }

    public Student(String firstName, String lastName, String emailAddress, Date dob) {
        super(firstName, lastName, emailAddress, dob);
        calendar = new Calendar();
        clubs = new ArrayList<>();
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public Club newClub(String name, String description) {
        return new Club(this, name, description);
    }

    public List<Club> getClubs() {
        return clubs;
    }

    public String consoleFormat(String prefix) {
        return prefix + "[S] " + super.consoleFormat("");
    }

    @Override
    public JSONElement toJSON() {
        JSONElement json = super.toJSON();
        json.putStructure("calendar", calendar.toJSON());
        json.putArray("clubs");
        for (Club club : clubs) {
            json.search("clubs").putPrimitive(club.getId().toString());
        }
        return json;
    }

    @Override
    public Student fromJSON(JSONElement json) {
        super.fromJSON(json);
        calendar.fromJSON(json.search("calendar"));
        clubs.clear();
        for (JSONElement e : json.search("clubs").getChildren()) {
            clubs.add(new Club(this).fromJSON(e));
        }
        return this;
    }
}
