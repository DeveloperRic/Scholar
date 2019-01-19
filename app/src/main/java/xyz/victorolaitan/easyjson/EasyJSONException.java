package xyz.victorolaitan.easyjson;

public class EasyJSONException extends Exception {

    static final int SAVE_ERROR = 0;
    static final int UNEXPECTED_TOKEN = 1;
    static final int LOAD_ERROR = 2;
    static final int ILLEGAL_ACCESS = 3;
    static final int INSTANTIATION_ERROR = 4;
    static final int FIELD_NOT_FOUND = 5;
    static final int FILE_NOT_JSON = 6;

    EasyJSONException(int error) {
        super(translateError(error));
    }

    EasyJSONException(int error, String details) {
        super(translateError(error) + " : " + details);
    }

    EasyJSONException(int error, Throwable cause) {
        super(translateError(error), cause);
    }

    EasyJSONException(int error, JSONElement source) {
        super(translateError(error) + formatErrorSource(source));
    }

    private static String translateError(int error) {
        switch (error) {
            case SAVE_ERROR:
                return "EasyJSON failed to save json structure!";
            case UNEXPECTED_TOKEN:
                return "EasyJSON encountered an incompatible token!";
            case LOAD_ERROR:
                return "A read error occurred when attempting to load the file!";
            case FILE_NOT_JSON:
                return "The file provided doesn't contain a valid JSON structure!";
            case ILLEGAL_ACCESS:
                return "All sub-classes and fields of the serializable class must be declared public when using the DatabaseHelper!";
            case INSTANTIATION_ERROR:
                return "The serializable class must be a static, instantiable class with a zero-parameter constructor!";
            case FIELD_NOT_FOUND:
                return "The serializable class must contain fields with names (and types) corresponding with the keyed elements of the EasyJSON structure!";
            default:
                return null;
        }
    }

    private static String formatErrorSource(JSONElement source) {
        String format = " : fail @ [" + source.getType() + "]'" + source.getKey() + "'";
        if (source.getParent() != null) {
            format += " : parent is [" + source.getParent().getType() + "]'" + source.getParent().getKey() + "'";
        }
        return format;
    }
}
