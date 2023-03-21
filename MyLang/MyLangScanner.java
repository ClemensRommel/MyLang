package MyLang;

import java.util.ArrayList;
import java.util.List;

public final class MyLangScanner {
    private List<Token> output = new ArrayList<>();
    private String source;
    private int current = 0;
    private int line;
    private int length;

    private MyLangScanner(String source, int inputLine) {
        this.source = source;
        this.length = source.length();
        this.line = inputLine;
    }

    public static List<Token> tokenize(String source) {
        return tokenize(source, 1);
    }

    public static List<Token> tokenize(String source, int inputLine) {
        var scanner = new MyLangScanner(source, inputLine);
        
        while(!scanner.atEnd()) {
            scanner.scan();
        }
        scanner.output.add(new Token(TokenType.EOF, "", scanner.line));
        return scanner.output;
    }

    private boolean atEnd() {
        return current >= length;
    }

    private void scan() {
        skipWhiteSpace();
        if(!atEnd()){
            var nextToken = getNextToken();
            output.add(nextToken);
        }
    }

    private void skipWhiteSpace() {
        while(!atEnd()) {
            if(Character.isWhitespace(peek())) {
                next();
            } else if(peek() == '/') {
                if(peekNext() == '/') {
                    while(!atEnd() && peek() != '\n') {
                        next();
                    }
                } else if(peekNext() == '*') {
                    next();
                    int depth = 1;
                    while(!atEnd() && depth > 0) {
                        if(peek() == '*') {
                            if(peekNext() == '/') {
                                depth--;
                                next(); next();
                            } else {
                                next();
                            }
                        } else if(peek() == '/' && peekNext() == '*') {
                            depth++;
                            next(); next();
                        } else {
                            next();
                        }
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    private Token getNextToken() {
        return switch(next()) {
            case ':' -> {
                if(match('=')) {
                    yield new Token(TokenType.ASSIGN, ":=", line);
                } else {
                    yield new Token(TokenType.COLON, ":", line);
                }
            }
            case ';' -> new Token(TokenType.SEMICOLON,";", line);
            case '(' -> new Token(TokenType.LPAREN,"(", line);
            case ')' -> new Token(TokenType.RPAREN,")", line);
            case '[' -> new Token(TokenType.LBRACKET,"[", line);
            case ']' -> new Token(TokenType.RBRACKET,"]", line);    
            case '{' -> new Token(TokenType.LBRACE,"{", line);
            case '}' -> new Token(TokenType.RBRACE,"}", line);
            case '.' -> {
                if(match('.')) {
                    yield new Token(TokenType.DOTS, "..", line);
                } else {
                    yield new Token(TokenType.DOT, ".", line);
                }
            }
            case ',' -> new Token(TokenType.COMMA,",", line);
            case '+' -> new Token(TokenType.PLUS,"+", line);
            case '-' -> new Token(TokenType.MINUS,"-", line);
            case '*' -> new Token(TokenType.STAR,"*", line);
            case '/' -> new Token(TokenType.SLASH,"/", line);
            case '%' -> new Token(TokenType.PERCENT,"%", line);
            case '!' -> {
                if(match('=')) {
                    yield new Token(TokenType.NOT_EQUAL, "!=", line);
                } else {
                    yield new Token(TokenType.BANG, "!", line);
                }
            }
            case '<' -> {
                if(match('=')) {
                    yield new Token(TokenType.LESS_EQUAL,"<=", line);
                } else {
                    yield new Token(TokenType.LESS,"<", line);
                }
            }
            case '>' -> {
                if(match('=')) {
                    yield new Token(TokenType.GREATER_EQUAL,">=", line);
                } else {
                    yield new Token(TokenType.GREATER,">", line);
                }
            }
            case '=' -> new Token(TokenType.EQUAL,"=", line);
            case '"' -> stringLiteral();
            default -> {
                if(Character.isDigit(previous())) {
                    yield numberLiteral();
                } else if(Character.isLetter(previous())) {
                    yield identifier();
                } else {
                    yield new Token(TokenType.ERROR, "" + previous(), line);
                }
            }
        };
    }

    private Token numberLiteral() {
        StringBuilder sb = new StringBuilder();
        sb.append(previous());
        while(Character.isDigit(peek())) {
            sb.append(next());
        }
        if(peek() == '.' && Character.isDigit(peekNext())) {
            next();
            sb.append(".");
            while(Character.isDigit(peek())) {
                sb.append(next());
            }
        }
        return new Token(TokenType.NUMBER_LITERAL, sb.toString(), line);
    }

    private Token identifier() {
        StringBuilder sb = new StringBuilder();
        sb.append(previous());
        while(Character.isLetterOrDigit(peek())) {
            sb.append(next());
        }
        var identifier = sb.toString();
        return identifierToken(identifier);
    }

    private Token identifierToken(String identifier) {
        if(Character.isLowerCase(identifier.charAt(0))) {
            return lowercaseIdentifier(identifier);
        } else {
            return uppercaseIdentifier(identifier);
        }
    }

    private Token lowercaseIdentifier(String identifier) {
        return switch(identifier) {
            case "true" -> new Token(TokenType.TRUE, "true", line);
            case "false" -> new Token(TokenType.FALSE, "false", line);
            case "null" -> new Token(TokenType.NULL, "null", line);
            case "this" -> new Token(TokenType.VALUE_THIS, "this", line);
            case "new" -> new Token(TokenType.NEW, "new", line);
            case "if" -> new Token(TokenType.IF, "if", line);
            case "then" -> new Token(TokenType.THEN, "then", line);
            case "else" -> new Token(TokenType.ELSE, "else", line);
            case "while" -> new Token(TokenType.WHILE, "while", line);
            case "for" -> new Token(TokenType.FOR, "for", line);
            case "in" -> new Token(TokenType.IN, "in", line);
            case "do" -> new Token(TokenType.DO, "do", line);
            case "yield" -> new Token(TokenType.YIELD, "yield", line);
            case "and" -> new Token(TokenType.AND, "and", line);
            case "or" -> new Token(TokenType.OR, "or", line);
            case "xor" -> new Token(TokenType.XOR, "xor", line);
            case "init" -> new Token(TokenType.INIT, "init", line);
            case "var" -> new Token(TokenType.VAR, "var", line);
            case "val" -> new Token(TokenType.VAL, "val", line);
            case "fun" -> new Token(TokenType.FUN, "fun", line);
            case "class" -> new Token(TokenType.CLASS, "class", line);
            default -> new Token(TokenType.VALUE_IDENTIFIER, identifier, line);
        };
    }

    private Token uppercaseIdentifier(String identifier) {
        return new Token(TokenType.TYPE_IDENTIFIER, identifier, line);
    }

    private Token stringLiteral() {
        StringBuilder builder = new StringBuilder();
        while(!atEnd() && next() != '"') {
            builder.append(previous());
        }
        if(atEnd() && previous() != '"') {
            return new Token(TokenType.ERROR, "Unterminated string literal", line);
        }

        return new Token(TokenType.STRING_LITERAL, builder.toString(), line);
    }

    private char next() {
        var r = source.charAt(current);
        current++;
        if(r == '\n') line++;
        return r;
    }

    private char peek() {
        if(current >= source.length()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if(current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private char previous() {
        return source.charAt(current - 1);
    }

    private boolean match(char expected) {
        if(atEnd()) {
            return false;
        }
        if(source.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    private void emitIfNext(char c, TokenType t, int lengthAlready) {
        if(match(c)) {
            output.add(new Token(t, source.substring(current - lengthAlready - 1, current + 1), line));
        } else {
            next();
            output.add(new Token(TokenType.ERROR, source.substring(current - lengthAlready - 1, current + 1), line));
        }
    }
}

