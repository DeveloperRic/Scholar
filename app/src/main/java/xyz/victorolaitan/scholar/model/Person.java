package xyz.victorolaitan.scholar.model;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.util.Indexable;
import xyz.victorolaitan.scholar.util.Observable;

import static java.text.DateFormat.getDateTimeInstance;

public class Person implements Observable, Indexable {

    private UUID personId = UUID.randomUUID();

    private String firstName;
    private String lastName;
    private String emailAddress;
    private Date dob;

    public Person() {
    }

    public Person(String firstName, String lastName, String emailAddress, Date dob) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.dob = dob;
    }

    @NonNull
    @Override
    public UUID getId() {
        return personId;
    }

    @Override
    public String getName() {
        return getFullName();
    }

    @Override
    public void setName(String name) {
        parseFullName(name);
    }

    @Override
    public String getFancyName() {
        return getFullName();
    }

    @Override
    public String getShortName() {
        return firstName;
    }

    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }

    public void parseFullName(String name) {
        if (name.contains(",")) {
            String[] parts = name.split(",");
            firstName = parts[0].trim();
            if (parts.length > 1) {
                lastName = (parts[0] + parts[1]).trim();
            } else {
                lastName = "";
            }
        }
        if (name.contains(" ")) {
            firstName = name.substring(0, name.indexOf(' ')).trim();
            lastName = name.substring(name.indexOf(' ') + 1).trim();
        }
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + firstName + lastName + " dob: " + getDateTimeInstance().format(dob);
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("personId", personId.toString());
        json.putPrimitive("fname", firstName);
        json.putPrimitive("lname", lastName);
        json.putPrimitive("dob", dob.getTime());
        return json.getRootNode();
    }

    @Override
    public Person fromJSON(JSONElement json) {
        personId = UUID.fromString(json.valueOf("personId"));
        firstName = json.valueOf("fname");
        lastName = json.valueOf("lname");
        dob = new Date(json.<Long>valueOf("dob"));
        return this;
    }

    @Override
    public int getCompareType() {
        return STRING;
    }

    @Override
    public java.lang.Comparable getCompareObject() {
        return getFullName();
    }
}
