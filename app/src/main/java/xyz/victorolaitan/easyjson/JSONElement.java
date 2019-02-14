package xyz.victorolaitan.easyjson;

import android.support.annotation.NonNull;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class JSONElement implements Iterable<JSONElement> {
    private EasyJSON easyJSONStructure;
    private JSONElement parent;
    private JSONElementType type;
    private ArrayList<JSONElement> children = new ArrayList<>();
    private String key;
    private Object value;

    JSONElement(EasyJSON easyJSONStructure, JSONElement parent, JSONElementType type, String key, Object value) {
        this.easyJSONStructure = easyJSONStructure;
        this.parent = parent;
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public EasyJSON getEasyJSONStructure() {
        return easyJSONStructure;
    }

    public JSONElement getParent() {
        return parent;
    }

    public JSONElementType getType() {
        return type;
    }

    public void setType(SafeJSONElementType type) {
        this.type = type.getRealType();
    }

    public ArrayList<JSONElement> getChildren() {
        return children;
    }

    public String getKey() {
        return key;
    }

    public <T> T getValue() {
        return (T) value;
    }

    public JSONElement putElement(JSONElement jsonElement) {
        return putElement(jsonElement.key, jsonElement);
    }

    public void putElement(JSONElement... elements) {
        for (JSONElement element : elements) {
            putElement(element.key, element);
        }
    }

    public JSONElement putElement(String key, JSONElement jsonElement) {
        switch (jsonElement.type) {
            case PRIMITIVE:
                return putPrimitive(key, jsonElement);
            case ARRAY:
                return putArray(key, jsonElement);
            case STRUCTURE:
                return putStructure(key, jsonElement);
            case ROOT:
                return putStructure(null, jsonElement);
        }
        return null;
    }

    public JSONElement putPrimitive(Object value) {
        JSONElement element;
        if (value instanceof JSONElement) {
            element = (JSONElement) value;
            element.parent = this;
        } else {
            element = new JSONElement(easyJSONStructure, this, JSONElementType.PRIMITIVE, null, value);
        }
        children.add(element);
        return element;
    }

    @NonNull
    public JSONElement putPrimitive(String key, Object value) {
        JSONElement search = search(key);
        if (search == null) {
            JSONElement element;
            if (value instanceof JSONElement) {
                element = (JSONElement) value;
                element.easyJSONStructure = easyJSONStructure;
                element.parent = this;
            } else {
                element = new JSONElement(easyJSONStructure, this, JSONElementType.PRIMITIVE, key, value);
            }
            children.add(element);
            return element;
        } else {
            if (value instanceof JSONElement) {
                search.merge((JSONElement) value);
            } else {
                search.value = value;
            }
            return search;
        }
    }

    public JSONElement putStructure(String key) {
        JSONElement element = search(key);
        if (element == null) {
            element = new JSONElement(easyJSONStructure, this, JSONElementType.STRUCTURE, key, null);
            children.add(element);
        } else {
            throw new RuntimeException("EasyJSON: An element already exists with that key!");
        }
        return element;
    }

    public JSONElement putStructure(String key, EasyJSON easyJSON) {
        easyJSON.getRootNode().easyJSONStructure = easyJSONStructure;
        easyJSON.getRootNode().parent = this;
        return putStructure(key, easyJSON.getRootNode());
    }

    public JSONElement putStructure(String key, JSONElement structure) {
        JSONElement searchResult = search(key);
        if (searchResult == null) {
            structure.type = JSONElementType.STRUCTURE;
            structure.key = key;
            claimElement(structure);
            return structure;
        } else {
            return searchResult.merge(structure);
        }
    }

    public JSONElement putArray(String key, Object... items) {
        JSONElement search = search(key);
        if (search == null || search.type != JSONElementType.ARRAY) {
            JSONElement element = new JSONElement(easyJSONStructure, this, JSONElementType.ARRAY, key, null);
            for (Object item : items) {
                if (item instanceof JSONElement) {
                    JSONElement itemElement = (JSONElement) item;
                    element.putElement(itemElement.getKey(), itemElement);
                } else {
                    element.putPrimitive(item);
                }
            }
            children.add(element);
            return element;
        } else {
            for (Object item : items) {
                if (item instanceof JSONElement) {
                    JSONElement itemElement = (JSONElement) item;
                    search.putElement(itemElement.getKey(), itemElement);
                } else {
                    search.putPrimitive(item);
                }
            }
            return search;
        }
    }

    public boolean elementExists(String... location) {
        return search(location) != null;
    }

    public JSONElement search(String... location) {
        return deepSearch(this, location, 0);
    }

    private JSONElement deepSearch(JSONElement element, String[] location, int locPosition) {
        for (int i = 0; locPosition < location.length && i < element.children.size(); i++) {
            JSONElement child = (JSONElement) element.children.get(i);
            if (child.key != null) {
                if (child.key.equals(location[locPosition])) {
                    if (locPosition == location.length - 1) {
                        return child;
                    } else {
                        return deepSearch(child, location, locPosition + 1);
                    }
                }
            }
        }
        return null;
    }

    public <T> T valueOf(String... location) {
        return (T) search(location).getValue();
    }

    public String stringValueOf(String defaultString, String... location) {
        String value;
        try {
            value = valueOf(location);
            if (value != null) {
                return value;
            }
        } catch (Exception ignored) {
        }
        return defaultString;
    }

    public <T> T valueOf(T defaultValue, String... location) {
        T value;
        try {
            value = valueOf(location);
            if (value != null) {
                return value;
            }
        } catch (Exception ignored) {
        }
        return defaultValue;
    }

    public void combine(EasyJSON easyJSONStructure) {
        combine(easyJSONStructure.getRootNode());
    }

    /**
     * overwrites any existing elements with keys
     * @param jsonElement
     */
    public void combine(JSONElement jsonElement) {
        for (int i = 0; i < jsonElement.children.size(); i++) {
            putElement((JSONElement) jsonElement.children.get(i));
        }
    }

    /**
     * may cause duplicate keys, use carefully!
     * @param jsonElement
     */
    private void claimElement(JSONElement jsonElement) {
        jsonElement.easyJSONStructure = easyJSONStructure;
        jsonElement.parent = this;
        children.add(jsonElement);
    }

    JSONElement merge(JSONElement newElement) {
        type = newElement.type;
        children = newElement.children;
        value = newElement.value;
        return this;
    }

    @NonNull
    @Override
    public Iterator<JSONElement> iterator() {
        return children.iterator();
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj = easyJSONStructure.deepSave(obj, this);
        } catch (EasyJSONException e) {
            e.printStackTrace();
            return super.toString();
        }
        return obj.toJSONString();
    }
}
