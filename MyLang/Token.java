package MyLang;

public record Token(TokenType type, String lexeme, int line) {
    public String toString() {
        return "| "+line+" "+type+" = \"" + lexeme + "\"";
    }
}
