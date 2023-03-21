package MyLang;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import MyLang.MyLangAST.NumericLiteral;

import static MyLang.MyLangAST.*;

public class MyLangParser {
    private List<Token> tokens;
    private int current = 0;
    private int length;

    public MyLangParser(String source) {
        tokens = MyLangScanner.tokenize(source);
        length = tokens.size();
    }
    public MyLangParser(List<Token> tokens) {
        this.tokens = tokens;
        length = tokens.size();
    }

    private boolean atEnd() {
        return current >= length || tokens.get(current).type() == TokenType.EOF;
    }

    private Token next() {
        if(atEnd()) { // keine tokens mehr? der letzte wird zurückgegeben - EOF
            return tokens.get(length - 1);
        }
        return tokens.get(current++);
    }

    private Token peek() {
        if(atEnd()) { // keine tokens mehr? der letzte wird zurückgegeben - EOF
            return tokens.get(length - 1);
        }
        return tokens.get(current);
    }
    private Token peekNext() {
        if(atEnd()) { // keine tokens mehr? der letzte wird zurückgegeben - EOF
            return tokens.get(length - 1);
        }
        return tokens.get(current + 1);
    }
    private Token previous() {
        if(current == 0) {
            throw new ParseError("no previous token", tokens.get(0).line());
        }
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type) {
        if(atEnd()) {
            if(type == TokenType.EOF) {
                return tokens.get(length-1);
            }
            throw new ParseError("Expected " + type + " but got EOF", tokens.get(length - 1).line());
        }
        if(peek().type() != type) {
            throw new ParseError("Expected " + type + ", got " + peek().type(), peek().line());
        }
        return next();
    }

    private boolean match(TokenType t) {
        if(atEnd()) {
            return false;
        }
        if(peek().type() != t) {
            return false;
        }
        next();
        return true;
    }

    public static Optional<MyLangAST> parse(String source) {
        var parser = new MyLangParser(source);
        return parser.parse();
    }

    public static Optional<MyLangAST> parse(List<Token> tokens) {
        var parser = new MyLangParser(tokens);
        return parser.parse();
    }

    public static Optional<MyLangProgram> parseProgram(String source) {
        var parser = new MyLangParser(source);
        List<DeclarationOrStatement> program = new ArrayList<>();
        try {
            while(!parser.atEnd()) {
                program.add(parser.parseDeclarationOrStatement());
            }
            return Optional.of(new MyLangProgram(program));
        } catch(ParseError error) {
            error.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<MyLangAST> parse() {
        try {
            var value = parseAny();
            consume(TokenType.EOF);
            return Optional.of(value);
        } catch(ParseError e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // parses expressions, statements and declarations
    private MyLangAST parseAny() {
        if(peek().type().startsDeclaration() && peekNext().type().isIdentifier()) {
            return parseDeclaration();
        } else {
            var expression = parseExpression();
            if(match(TokenType.SEMICOLON)) {
                return new ExpressionStatement(expression);
            } else if(match(TokenType.ASSIGN)) {
                var rightHandSide = parseExpression();
                consume(TokenType.SEMICOLON);
                return makeAssigment(expression, rightHandSide);
            } else {
                return expression;
            }
        }
    }

    private DeclarationOrStatement parseDeclarationOrStatement() {
        if(peek().type().startsDeclaration() && peekNext().type().isIdentifier()) {
            return parseDeclaration();
        } else {
            var expression = parseExpression();
            if(match(TokenType.ASSIGN)) {
                var rightHandSide = parseExpression();
                consume(TokenType.SEMICOLON);
                return makeAssigment(expression, rightHandSide);
            } else {
                consume(TokenType.SEMICOLON);
                return new ExpressionStatement(expression);
            }
        }
    }

    private ConstructorOrDeclaration parseDeclarationOrConstructor(String typeName) {
        if(peek().type().startsDeclaration()) {
            return parseDeclaration();
        } else {
            return parseConstructor(typeName);
        }
    }

    private Constructor parseConstructor(String typeName) {
        var declarationType = next();
        if(declarationType.type() == TokenType.INIT) {
            return finalizeClassConstructor(typeName);
        } else {
            throw new ParseError("Invalid constructor declaration start: "+declarationType.type(), declarationType.line());
        }
    }

    private ClassConstructor finalizeClassConstructor(String typeName) {
        var keyword = previous();
        consume(TokenType.LPAREN);
        var parameters = parseParameters();
        consume(TokenType.ASSIGN);
        var body = parseExpression();
        consume(TokenType.SEMICOLON);
        return new ClassConstructor(keyword, parameters, body);
    }

    private Declaration parseDeclaration() {
        var declarationType = next();
        return switch(declarationType.type()) {
            case VAR -> finalizeVariableDeclaration(true);
            case VAL -> finalizeVariableDeclaration(false);
            case FUN -> finalizeFunctionDeclaration();
            case CLASS -> finalizeClassDeclaration();
            default -> throw new ParseError("Unknown declaration type: " + declarationType.type(), declarationType.line());
        };
    }

    private Declaration finalizeVariableDeclaration(boolean isReassignable) {
        var name = consume(TokenType.VALUE_IDENTIFIER);
        consume(TokenType.ASSIGN);
        var initializer = parseExpression();
        consume(TokenType.SEMICOLON);
        return new VariableDeclaration(name, initializer, isReassignable);
    }
    private Declaration finalizeFunctionDeclaration() {
        var name = consume(TokenType.VALUE_IDENTIFIER);
        var expression = finalizeFunctionExpressionWithName(name.lexeme(), 1);
        return new FunctionDeclaration(name, expression.parameters(), expression.body());
    }
    private FunctionExpression finalizeFunctionExpressionWithName(String name, int counter) {
        consume(TokenType.LPAREN);
        var parameters = parseParameters();
        // consume(TokenType.RPAREN); // done by parseParameters
        if(peek().type() == TokenType.LPAREN) {
            return new FunctionExpression(name+"$"+counter, parameters, finalizeFunctionExpressionWithName(name, counter+1));
        }
        consume(TokenType.ASSIGN);
        var body = parseExpression();
        consume(TokenType.SEMICOLON);
        return new FunctionExpression(name, parameters, body);
    }

    private Declaration finalizeClassDeclaration() {
        var name = consume(TokenType.TYPE_IDENTIFIER);
        consume(TokenType.ASSIGN);
        consume(TokenType.LBRACE);
        List<Declaration> members = new ArrayList<>();
        ClassConstructor constructor = null;
        while(!match(TokenType.RBRACE)) {
            int line = peek().line();
            ConstructorOrDeclaration declaration = parseDeclarationOrConstructor(name.lexeme());
            if(declaration instanceof ClassConstructor c) {
                constructor = c;
            } else if(declaration instanceof Declaration d) {
                members.add(d);
            } else {
                throw new ParseError("Invalid Constructor type for Class Declaration: " + declaration.getClass(), line);
            }
        }
        consume(TokenType.SEMICOLON);
        return new ClassDeclaration(name, members, constructor);
    }

    private List<Token> parseParameters() {
        List<Token> parameters = new ArrayList<>();
        if(!match(TokenType.RPAREN)) {
            do {
                parameters.add(consume(TokenType.VALUE_IDENTIFIER));
            } while(match(TokenType.COMMA));
            consume(TokenType.RPAREN);
        }
    
        return parameters;
    }


    private Statement makeAssigment(Expression leftHandSide, Expression rightHandSide) {
        if(leftHandSide instanceof Identifier ident) {
            return new SetStatement(ident.value(), rightHandSide);
        } else if(leftHandSide instanceof IndexExpression index) {
            return new SetIndexStatement(index.list(), index.index(), rightHandSide);
        } else if(leftHandSide instanceof PropertyExpression property) {
            return new SetPropertyStatement(property.object(), property.name(), rightHandSide);
        } else {
            throw new ParseError("Expected Identifier as Assigment target", -1);
        }
    }

    private Expression parseExpression() {
        return booleanCombination();
    }

    private Expression booleanCombination() {
        Expression left = equality();
        while(match(TokenType.OR) || match(TokenType.AND) || match(TokenType.XOR)) {
            Token operator = previous();
            Expression right = equality();
            left = new BinaryOperation(operator, left, right);
        }
        return left;
    }

    private Expression equality() {
        Expression left = comparison();
        while(match(TokenType.NOT_EQUAL) || match(TokenType.EQUAL)) {
            var operator = previous();
            var right = comparison();
            left = new BinaryOperation(operator, left, right);
        }
        return left;
    }

    // 100% KI-Generiert
    private Expression comparison() {
        Expression left = addition();
        while(match(TokenType.GREATER) || match(TokenType.GREATER_EQUAL) || match(TokenType.LESS) || match(TokenType.LESS_EQUAL) || match(TokenType.IN)) {
            var operator = previous();
            var right = addition();
            left = new BinaryOperation(operator, left, right);
        }
        return left;
    }

    private Expression addition() {
        var left = multiplication();
        while(match(TokenType.PLUS) || match(TokenType.MINUS)) {
            var op = previous();
            var right = multiplication();
            left = new BinaryOperation(op, left, right);
        }
        return left;
    }

    private Expression multiplication() {
        var left = unaryExpression();
        while(match(TokenType.STAR) || match(TokenType.SLASH) || match(TokenType.PERCENT)) {
            var operator = previous();
            var right = unaryExpression();
            left = new BinaryOperation(operator, left, right);
        }
        return left;
    }

    private Expression unaryExpression() {
        if(match(TokenType.MINUS) || match(TokenType.BANG) || match(TokenType.PLUS)) {
            var operator = previous();
            var right = unaryExpression();
            return new UnaryOperation(operator, right);
        } else {
            return propertyOrCall();
        }
    }

    private Expression propertyOrCall() {
        Expression left = primary();
        while(match(TokenType.LPAREN) || match(TokenType.LBRACKET) || match(TokenType.DOT)) {
            var operator = previous();
            if(operator.type() == TokenType.DOT) {
                var property = consume(TokenType.VALUE_IDENTIFIER);
                left = new PropertyExpression(left, property);
            } else if(operator.type() == TokenType.LPAREN) {
                var parameters = parseCommaSeparated(TokenType.RPAREN);
                left = new FunctionCall(left, parameters);
            } else if(operator.type() == TokenType.LBRACKET) { // Index
                var index = parseExpression();
                consume(TokenType.RBRACKET);
                left = new IndexExpression(left, index);
            } else {
                throw new ParseError("Invalid call operator", previous().line());
            }
        }
        return left;
    }

    private Expression primary() {
        if(match(TokenType.LPAREN)) {
            var expr = parseExpression();
            consume(TokenType.RPAREN);
            return expr;
        } else if(match(TokenType.IF)) {
            var condition = parseExpression();
            consume(TokenType.THEN);
            var thenBranch = parseExpression();
            consume(TokenType.ELSE);
            var elseBranch = parseExpression();
            return new IfExpression(condition, thenBranch, elseBranch);
        } else if(match(TokenType.WHILE)) {
            var condition = parseExpression();
            if(match(TokenType.DO)) {
                var body = parseStatementOrExpression();
                return new WhileDoExpression(condition, body);
            } else {
                consume(TokenType.YIELD);
                var body = parseExpression();
                return new WhileYieldExpression(condition, body);
            }
        } else if(match(TokenType.FOR)) {
            var variableName = consume(TokenType.VALUE_IDENTIFIER);
            consume(TokenType.IN);
            var collection = parseExpression();
            Expression guard = new BooleanLiteral(true);
            if(match(TokenType.IF)) {
                guard = parseExpression();
            }
            if(match(TokenType.DO)) {
                var body = parseStatementOrExpression();
                return new ForDoExpression(variableName, collection, guard, body);
            } else {
                consume(TokenType.YIELD);
                var body = parseExpression();
                return new ForYieldExpression(variableName, collection, guard, body);
            }
        } else if(match(TokenType.LBRACE)) {
            return finishBlockExpression();
        } else if(match(TokenType.LBRACKET)) {
            return finishListExpression();
        } else if(match(TokenType.FUN)) {
            return finishFunctionExpression();
        }
        return someIdentifierOrNew();
    }

    private Expression someIdentifierOrNew() {
        if(match(TokenType.VALUE_IDENTIFIER)) {
            return new Identifier(previous());
        } else if(match(TokenType.NEW)) {
            var className = consume(TokenType.TYPE_IDENTIFIER);
            consume(TokenType.LPAREN);
            var parameters = parseCommaSeparated(TokenType.RPAREN);
            return new FunctionCall(new Identifier(className), parameters);
        } else if(match(TokenType.VALUE_THIS)) {
            return new ThisExpression(previous());
        } else {
            return literal();
        }
    }

    private Statement parseStatementOrExpression() {
        var left = parseExpression();
        Statement resulting;
        if((match(TokenType.ASSIGN))) {
            var rightHandSide = parseExpression();
            resulting = makeAssigment(left, rightHandSide);
        } else {
            resulting = new ExpressionStatement(left);
        }
        return resulting;
    }

    private Expression finishListExpression() {
        if(match(TokenType.RBRACKET)) {
            return new ListExpression(List.of());
        }
        var first = parseExpression();
        if(match(TokenType.DOTS)) {
            var second = parseExpression();
            Expression step =  new NumericLiteral(1);
            if(match(TokenType.COLON)) {
                step = parseExpression();
            }
            consume(TokenType.RBRACKET);
            return new RangeExpression(first, step, second);
        } else if(match(TokenType.RBRACKET)) {
            return new ListExpression(List.of(first));
        } else {
            consume(TokenType.COMMA);
        }
        var items = parseCommaSeparated(TokenType.RBRACKET);
        items.add(0, first);
        return new ListExpression(items);
    }

    private Expression finishFunctionExpression() {
        consume(TokenType.LPAREN);
        var parameters = parseParameters();
        Expression body;
        if(peek().type() == TokenType.LPAREN) {
            body = finishFunctionExpression();
        } else {
            consume(TokenType.ASSIGN);
            body = parseExpression();
        }
        return new FunctionExpression("Anonymous Function", parameters, body);
    }

    private Expression finishBlockExpression() {
        List<DeclarationOrStatement> statements = new ArrayList<>();
        while(!match(TokenType.RBRACE)) {
            var next = parseAny();
            if(next instanceof Expression finalExpression) {
                consume(TokenType.RBRACE);
                return new BlockExpression(statements, finalExpression);
            } else {
                statements.add((DeclarationOrStatement) next);
            }
        }

        return new BlockExpression(statements, new NullLiteral(previous()));
    }

    private List<Expression> parseCommaSeparated(TokenType endDelimiter) {
        List<Expression> expressions = new ArrayList<>();
        if(!match(endDelimiter)) {
            do {
                expressions.add(parseExpression());
            } while(match(TokenType.COMMA));
            consume(endDelimiter);
        }
        return expressions;
    }

    private Expression literal() {
        if(match(TokenType.NUMBER_LITERAL)) {
            return new NumericLiteral(Double.parseDouble(previous().lexeme()));
        } else if(match(TokenType.STRING_LITERAL)) {
            return new StringLiteral(previous().lexeme());
        } else if(match(TokenType.TRUE)) {
            return new BooleanLiteral(true);
        } else if(match(TokenType.FALSE)) {
            return new BooleanLiteral(false);
        } else if(match(TokenType.NULL)) {
            return new NullLiteral(previous());
        } else {
            throw new ParseError("Expected literal, got "+next().type(), next().line());
        }
    }
}
