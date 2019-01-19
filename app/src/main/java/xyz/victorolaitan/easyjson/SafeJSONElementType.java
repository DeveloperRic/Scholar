package xyz.victorolaitan.easyjson;

public enum SafeJSONElementType {
    PRIMITIVE(JSONElementType.PRIMITIVE),
    ARRAY(JSONElementType.ARRAY),
    STRUCTURE((JSONElementType.STRUCTURE));

    private JSONElementType realType;

    SafeJSONElementType(JSONElementType realType) {
        this.realType = realType;
    }

    public JSONElementType getRealType() {
        return realType;
    }
}
