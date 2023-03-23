package MyLang;

public enum TokenType {
    LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE, SEMICOLON,
    DOT, DOTS, COMMA, COLON, ASSIGN, PLUS, MINUS, STAR, SLASH, PERCENT, BANG, EXPO,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL,

    NULL, TRUE, FALSE, STRING_LITERAL, NUMBER_LITERAL,

    AND, OR, XOR,


    VALUE_IDENTIFIER, TYPE_IDENTIFIER,

    VAR, VAL, VALUE_THIS, NEW, FUN, IF, THEN, ELSE, WHILE, FOR, IN, DO, YIELD,
    INIT, CLASS, MODULE, IMPORT, EXPORT, LOCAL,


    EOF, ERROR;

    public boolean startsDeclaration() {
        return this == VAR || this == VAL || this == FUN || this == CLASS|| this == MODULE || this == IMPORT || this == EXPORT;
    }

    public boolean isIdentifier() {
        return this == VALUE_IDENTIFIER || this == TYPE_IDENTIFIER;
    }

    public boolean shortCircuits() {
        return this == AND || this == OR;
    }
}
