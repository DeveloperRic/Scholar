package xyz.victorolaitan.scholar.model;

import java.util.List;

import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.model.subject.Subject;
import xyz.victorolaitan.scholar.util.exception.UnsupportedFunctionCallException;

public class ClubCalendar extends Calendar {

    @Override
    public List<Subject> getSubjects() {
        throw new UnsupportedFunctionCallException();
    }

    @Override
    public ClubCalendar fromJSON(JSONElement json) {
        return (ClubCalendar) super.fromJSON(json);
    }
}
