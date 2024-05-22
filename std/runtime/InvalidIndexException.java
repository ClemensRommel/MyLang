package std.runtime;

public class InvalidIndexException extends RuntimeException {
    public InvalidIndexException(String idx) {
        super(idx+" is not a valid index");
    }
}
