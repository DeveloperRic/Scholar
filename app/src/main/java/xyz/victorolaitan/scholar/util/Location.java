package xyz.victorolaitan.scholar.util;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.JSONElement;

public class Location implements ScholarModel {

    @Override
    public String consoleFormat(String prefix) {
        return "";
    }

    @Override
    public JSONElement toJSON() {
        return EasyJSON.create().getRootNode();
    }

    @Override
    public Location fromJSON(JSONElement json) {
        return this;
    }
}
