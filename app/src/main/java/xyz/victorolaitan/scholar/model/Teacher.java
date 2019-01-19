package xyz.victorolaitan.scholar.model;

import java.util.Date;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;

public class Teacher extends Person {

    public Teacher() {
        super();
    }

    public Teacher(String firstName, String lastName, String emailAddress, Date dob) {
        super(firstName, lastName, emailAddress, dob);
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + "[T] " + super.consoleFormat("");
    }

    @Override
    public Teacher fromJSON(JSONElement json) {
        return (Teacher) super.fromJSON(json);
    }
}
