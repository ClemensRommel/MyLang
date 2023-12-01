package std.codegen;

public class Functions {
    // Ignore a value. Used for expression statements and main()
    public static <A> void ignore(A a) {

    }

    public static int __toIndex(double d) {
        if (d % 1 == 0) {
            return (int) d;
        } else {
            throw new InvalidIndexException(d);
        }
    }
}
