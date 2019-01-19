package xyz.victorolaitan.scholar.model.subject;

import android.support.annotation.NonNull;

import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.util.Schedule;

public class Deliverable implements EvalComponent {
    private Evaluation evaluation;

    private UUID deliverableId = UUID.randomUUID();
    private String title;
    private String description;
    private Schedule deadline;

    Deliverable(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    Deliverable(Evaluation evaluation, String title, String description, @NonNull Schedule deadline) {
        this.evaluation = evaluation;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public UUID getDeliverableId() {
        return deliverableId;
    }

    @Override
    public String getFancyName() {
        return getName();
    }

    @Override
    public String getShortName() {
        return "Deliverable";
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
        return deadline;
    }

    @Override
    public String consoleFormat(String prefix) {
        return prefix + title + " due " + deadline.consoleFormat("") + "\n";
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("deliverableId", deliverableId.toString());
        json.putPrimitive("title", title);
        json.putPrimitive("description", description);
        json.putStructure("deadline", deadline.toJSON());
        return json.getRootNode();
    }

    @Override
    public Deliverable fromJSON(JSONElement json) {
        deliverableId = UUID.fromString(json.valueOf("deliverableId"));
        title = json.valueOf("title");
        description = json.valueOf("description");
        deadline = new Schedule().fromJSON(json.search("deadline"));
        return this;
    }

    @Override
    public int getCompareType() {
        return COMPLEX;
    }

    @Override
    public java.lang.Comparable getCompareObject() {
        return deadline;
    }
}
