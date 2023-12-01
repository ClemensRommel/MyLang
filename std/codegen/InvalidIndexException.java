package std.codegen;

public class InvalidIndexException extends RuntimeException {
    public InvalidIndexException(double idx) {
        super(idx+" is not a valid index");
        this.idx = idx;
    }

    double idx;
}
