package MyLang;

public class ParseError extends RuntimeException {
    public ParseError(String message, int line) {
        super("["+line+"]: "+message);
        System.out.println(getMessage());
    }
}
