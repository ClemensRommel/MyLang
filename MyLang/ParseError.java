package MyLang;

public class ParseError extends RuntimeException {
    public ParseError(String message, int line, String fileName) {
        super(fileName+"["+line+"]: "+message);
        System.out.println(getMessage());
    }
}
