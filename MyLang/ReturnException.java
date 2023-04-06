package MyLang;

public class ReturnException extends RuntimeException {
    Object returnValue;
    public ReturnException(Object value) {
        this.returnValue = value;
    }
}
