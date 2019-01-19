package xyz.victorolaitan.easyjson;

public class UnexpectedTokenException extends EasyJSONException {

    UnexpectedTokenException(String details) {
        super(UNEXPECTED_TOKEN, details);
    }
}
