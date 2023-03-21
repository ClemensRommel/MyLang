package MyLang;

public record MyLangRange(double start, double end, double step) {
    public String toString() {
        if(step == 1) {
            return "[" + start + ".." + end + "]";
        } else {
            return "[" + start + ".." + end + ":" + step + "]";
        }
    }
}
