package xyz.victorolaitan.scholar.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.util.Indexable;
import xyz.victorolaitan.scholar.util.Nameable;

public class Club implements Indexable, Nameable {
    private UUID id = UUID.randomUUID();
    private Student owner;

    private String name;
    private String description;
    private ClubCalendar calendar;

    private List<UUID> memberIds;

    public Club(Student owner) {
        this.owner = owner;
        calendar = new ClubCalendar();
        memberIds = new ArrayList<>();
    }

    Club(Student owner, String name, String description) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        calendar = new ClubCalendar();
        memberIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public UUID getId() {
        return id;
    }

    public Student getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getFancyName() {
        return name;
    }

    @Override
    public String getShortName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<UUID> getMemberIds() {
        return memberIds;
    }

    public void addMember(Student member) {
        this.memberIds.add(member.getId());
    }

    public void removeMember(Student member) {
        memberIds.remove(member.getId());
    }

    public ClubCalendar getCalendar() {
        return calendar;
    }

    @Override
    public String consoleFormat(String prefix) {
        return "";
    }

    @Override
    public JSONElement toJSON() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("id", id.toString());
        json.putPrimitive("name", name);
        json.putPrimitive("description", description);
        json.putStructure("calendar", calendar.toJSON());
        json.putArray("memberIds");
        for (UUID memberId : memberIds) {
            json.search("memberIds").putPrimitive(memberId.toString());
        }
        return json.getRootNode();
    }

    @Override
    public Club fromJSON(JSONElement json) {
        id = UUID.fromString(json.valueOf("id"));
        name = json.valueOf("name");
        description = json.valueOf("description");
        calendar = new ClubCalendar().fromJSON(json.search("calendar"));
        memberIds.clear();
        for (JSONElement e : json.search("memberIds").getChildren()) {
            memberIds.add(UUID.fromString(e.<String>getValue()));
        }
        return this;
    }
}
