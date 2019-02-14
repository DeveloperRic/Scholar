package xyz.victorolaitan.scholar.model.subject;

import android.support.annotation.NonNull;

import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.util.Schedule;

public class Test implements EvalComponent {
    private Evaluation evaluation;

    private UUID testId = UUID.randomUUID();
    private String title;
    private String description;
    private Schedule date;
    private float overallContribution = 0;

    Test(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    Test(Evaluation evaluation, String title, String description, @NonNull Schedule date) {
        this.evaluation = evaluation;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    @Override
    public Evaluation getEvaluation() {
        return evaluation;
    }

    public UUID getTestId() {
        return testId;
    }

    @Override
    public String getFancyName() {
        return getName();
    }

    @Override
    public String getShortName() {
        return "Test";
    }

    @Override
    public String getName() {
        return title;
    }

    public void setName(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Schedule getSchedule() {
        return date;
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + title + " on " + date.consoleFormat("") + "\n";
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("testId", testId.toString());
        json.putPrimitive("title", title);
        json.putPrimitive("description", description);
        json.putStructure("date", date.toJSON());
        json.putPrimitive("overallContribution", overallContribution);
        return json.getRootNode();
    }

    @Override
    public Test fromJSON(JSONElement json) {
        testId = UUID.fromString(json.valueOf("testId"));
        title = json.valueOf("title");
        description = json.valueOf("description");
        date = new Schedule().fromJSON(json.search("date"));
        overallContribution = json.valueOf(0, "overallContribution");
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

    @Override
    public float getOverallContribution() {
        return overallContribution;
    }

    @Override
    public void setOverallContribution(float percent) {
        overallContribution = percent;
    }
}
