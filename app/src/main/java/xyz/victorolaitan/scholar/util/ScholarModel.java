package xyz.victorolaitan.scholar.util;

import xyz.victorolaitan.easyjson.JSONElement;

public interface ScholarModel {

    String consoleFormat(String prefix);

    JSONElement toJSON();

    Object fromJSON(JSONElement json);
}
