package xyz.victorolaitan.scholar.model;

import java.util.Date;
import java.util.UUID;

import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.session.DatabaseLink;
import xyz.victorolaitan.scholar.util.ModelCollection;

public class Student extends Person {
    private Calendar calendar;
    private ModelCollection<Club> clubs;

    public Student() {
        calendar = new Calendar();
        initClubs();
    }

    public Student(String firstName, String lastName, String emailAddress, Date dob) {
        super(firstName, lastName, emailAddress, dob);
        calendar = new Calendar();
        initClubs();
    }

    private void initClubs() {
        clubs = new ModelCollection<Club>() {
            @Override
            protected Club getMethod(DatabaseLink link, UUID id) {
                return link.getClub(id, Student.this);
            }

            @Override
            protected boolean postMethod(DatabaseLink link, Club model) {
                return link.postClub(model);
            }
        };
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public Club newClub(String name, String description) {
        Club c = new Club(this, name, description);
        clubs.add(c);
        return c;
    }

    public ModelCollection<Club> getClubs() {
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
            clubs.add(UUID.fromString(e.getValue()));
        }
        return this;
    }
}
