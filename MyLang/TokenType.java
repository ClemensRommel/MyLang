package MyLang;

public enum TokenType {
    LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE, SEMICOLON,
    DOT, DOTS, COMMA, COLON, ASSIGN, PLUS, MINUS, STAR, SLASH, PERCENT, BANG, EXPO,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL,
    QUESTION_MARK,

    END, WHERE, DO,

    NULL, TRUE, FALSE, STRING_LITERAL, NUMBER_LITERAL,

    AND, OR, XOR,


    IDENTIFIER,

    // Value kexwords
    VAR, VAL, VALUE_THIS, NEW, FUN, IF, THEN, ELSE, WHILE, FOR, IN, YIELD,
    RETURN, MATCH, CASE,

    // Namespacing Keywords
    MODULE, IMPORT, EXPORT, LOCAL,

    // Type Keywords
    CLASS, TYPE_FUN, NUMBER, BOOLEAN, STRING, VOID, TYPE, ENUM,


    EOF, ERROR;

    public boolean startsDeclaration() {
        return  this == VAR || 
                this == VAL || 
                this == FUN || 
                this == CLASS || 
                this == MODULE || 
                this == IMPORT || 
                this == EXPORT || 
                this == SEMICOLON ||
                this == ENUM;
    }

    public boolean isVisibilityDeclaration() {
        return this == EXPORT || this == LOCAL;
    }

    public boolean isIdentifier() {
        return this == IDENTIFIER;
    }

    public boolean shortCircuits() {
        return this == AND || this == OR;
    }
}
