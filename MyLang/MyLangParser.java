package MyLang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import MyLang.MyLangAST.NumericLiteral;

import static MyLang.MyLangAST.*;

public class MyLangParser {
    private List<Token> tokens;
    private int current = 0;
    private int length;
    private boolean inClass = false;

    private boolean defaultVisibility = false;

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

    public static Optional<MyLangFile> parseFile(String source) {
        var parser = new MyLangParser(source);
        
        try {
            List<Declaration> program = new ArrayList<>();
            if(parser.match(TokenType.MODULE)){
                program.add(parser.moduleDeclaration());
            }
            List<Import> imports = parser.parseImports();
            //System.out.println(imports);
            while(!parser.atEnd()) {
                program.add(parser.parseDeclaration());
            }
            var file = new MyLangFile(program, imports);
            return Optional.of(file);
        } catch(ParseError error) {
            error.printStackTrace();
            return Optional.empty();
        }
    }

    private List<Import> parseImports() {
        List<Import> imports = new ArrayList<>();

        while(match(TokenType.IMPORT)) {
            imports.add(finalizeImportDeclaration());
        }
        
        return imports;
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
        if(peek().type().isVisibilityDeclaration() || 
                (peek().type().startsDeclaration() && peekNext().type().isIdentifier())) {
            return parseDeclaration();
        } else if(match(TokenType.SEMICOLON)) {
            return new EmptyDeclaration(previous());
        } else {
            var start = current;
            if(match(TokenType.IF)) {
                var condition = parseExpression();
                if(match(TokenType.DO)) {
                    var body = parseStatementOrExpression();
                    consume(TokenType.SEMICOLON);
                    return new IfStatement(condition, body);
                } else { // Backtrack and instead parse a expression
                    current = start;
                }
            } else if(match(TokenType.RETURN)) {
                var body = parseExpression();
                consume(TokenType.SEMICOLON);
                return new ReturnStatement(body);
            }
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

    private ModuleDeclaration moduleDeclaration() {
        var name = parsePath();
        consume(TokenType.SEMICOLON);
        return new ModuleDeclaration(name);
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
            throw new ParseError("Invalid constructor declaration start: "+declarationType.type(), 
                    declarationType.line());
        }
    }

    private ClassConstructor finalizeClassConstructor(String typeName) {
        var keyword = previous();
        consume(TokenType.LPAREN);
        var parameters = parseParameters();
        consume(TokenType.RPAREN);
        consume(TokenType.ASSIGN);
        var body = parseExpression();
        consume(TokenType.SEMICOLON);
        return new ClassConstructor(keyword, parameters, body);
    }

    private Type parseType() {
        Type left = primaryType();
        while(true) {
            if(match(TokenType.DOT)) {
                var accessed = consume(TokenType.IDENTIFIER);
                left = new Access(left, accessed);
            } else if(match(TokenType.LBRACKET)) {
                consume(TokenType.RBRACKET);
                left = new ListOf(left);
            } else {
                break;
            }
        }
        return left;
    }

    private Type primaryType() {
        if(match(TokenType.IDENTIFIER)) {
            return new TypeIdentifier(previous());
        } else if(match(TokenType.NUMBER)) {
            return new NumberType();
        } else if(match(TokenType.BOOLEAN)) {
            return new BooleanType();
        } else if(match(TokenType.LPAREN)) {
            var inner = parseType();
            consume(TokenType.RPAREN);
            return inner;
        } else if(match(TokenType.STRING)) {
            return new StringType();
        } else if(match(TokenType.VOID)) {
            return new VoidType();
        } else {
            consume(TokenType.TYPE_FUN);
            consume(TokenType.LPAREN);
            List<Type> parameters = new ArrayList<>();
            Map<String, Type> named = new HashMap<>();
            List<Type> optionalParams = new ArrayList<>();
            Map<String, Type> optionalNamed = new HashMap<>();
            Type varargsType = null;
            if(!match(TokenType.RPAREN)) {
                do {
                    if(peek().type() == TokenType.LBRACKET) {
                        break;
                    }
                    if(peek().type() == TokenType.LBRACE) { // Named Params
                        break;
                    }
                    var nextType = parseType();
                    if(match(TokenType.DOTS)) {
                        varargsType = nextType;
                        break;
                    } else {
                        parameters.add(nextType);
                    }
                } while(match(TokenType.COMMA));
                if(match(TokenType.LBRACKET)) {
                    if(!match(TokenType.RBRACKET)) {
                        do {
                            optionalParams.add(parseType());
                        } while(match(TokenType.COMMA));
                        consume(TokenType.RBRACKET);
                        match(TokenType.COMMA);
                    }
                }
                if(match(TokenType.LBRACE)) { // Named Params
                    if(!match(TokenType.RBRACE)) {
                        do {
                            var name = consume(TokenType.IDENTIFIER);
                            var isOptional = match(TokenType.QUESTION_MARK);
                            consume(TokenType.COLON);
                            var type = parseType();
                            if(isOptional) {
                                optionalNamed.put(name.lexeme(), type);
                            } else {
                                named.put(name.lexeme(), type);
                            }
                        } while(match(TokenType.COMMA));
                        consume(TokenType.RBRACE);
                    }
                }
                consume(TokenType.RPAREN);
            }
            consume(TokenType.COLON);
            var returnType = parseType();
            return new FunctionType(
                    parameters,
                    optionalParams,
                    named,
                    optionalNamed,
                    varargsType,
                    returnType
                    );
        }

    }

    private Declaration parseDeclaration() {
        boolean export = defaultVisibility;
        if(peek().type().isVisibilityDeclaration()) {
            if((match(TokenType.EXPORT))) {
                export = true;
            } else if(match(TokenType.LOCAL)) {
                export = false;
            } else {
                throw new ParseError("Unknown visibility declaration: " + peek().type(), 
                        peek().line());
            }
        }
        var declarationType = next();
        return switch(declarationType.type()) {
            case VAR -> finalizeVariableDeclaration(true, export);
            case VAL -> finalizeVariableDeclaration(false, export);
            case FUN -> finalizeFunctionDeclaration(export);
            case CLASS -> finalizeClassDeclaration(export);
            case TYPE -> finalizeTypeDefDeclaration(export);
            case SEMICOLON -> new EmptyDeclaration(declarationType);
            default -> throw new ParseError("Unknown declaration type: " + declarationType.type(), 
                    declarationType.line());
        };
    }

    private Declaration finalizeTypeDefDeclaration(boolean export) {
        var name = consume(TokenType.IDENTIFIER);
        consume(TokenType.ASSIGN);
        var def = parseType();
        consume(TokenType.SEMICOLON);
        return new TypeDefDeclaration(name, def, export);
    }

    private Import finalizeImportDeclaration() {
        var name = parsePath();
        consume(TokenType.SEMICOLON);
        return new ImportDeclaration(name);
    }

    private Declaration finalizeVariableDeclaration(boolean isReassignable, boolean export) {
        var name = consume(TokenType.IDENTIFIER);
        consume(TokenType.COLON);
        var type = parseType();
        Expression initializer;
        if(inClass && match(TokenType.SEMICOLON)) {
            initializer = new NullLiteral(previous());
        } else {
            consume(TokenType.ASSIGN);
            initializer = parseExpression();
            consume(TokenType.SEMICOLON);
        }
        return new VariableDeclaration(
                name, 
                type, 
                initializer, 
                isReassignable, 
                export);
    }
    private Declaration finalizeFunctionDeclaration(boolean export) {
        var name = consume(TokenType.IDENTIFIER);
        var expression = finalizeFunctionExpressionWithName(name.lexeme(), 1);
        return new FunctionDeclaration(
                name, 
                expression.parameters(), 
                expression.body(), 
                expression.retType(), 
                export);
    }
    private FunctionExpression finalizeFunctionExpressionWithName(String name, int counter) {
        consume(TokenType.LPAREN);
        var parameters = parseParameters();
        consume(TokenType.RPAREN); 
        if(peek().type() == TokenType.LPAREN) {
            var next = finalizeFunctionExpressionWithName(name, counter+1);
            return new FunctionExpression(
                    name+"$"+counter, 
                    parameters, 
                    next, 
                    new FunctionType(
                        next.parameters().types(), 
                        next.parameters().optionals().stream().map(OptionalParam::type).toList(),
                        next.parameters().named(),
                        next.parameters().optionalNamed().entrySet().stream()
                            .collect(Collectors.toMap(
                                entry -> entry.getKey(), 
                                entry -> entry.getValue().type())),
                        next.parameters().varargsType(), 
                        next.retType()));
        }
        Type resultType;
        if(match(TokenType.COLON)) {
            resultType = parseType();
        } else {
            resultType = new VoidType();
        }
        Expression body;
        if(match(TokenType.LBRACE)) {
            body = finishBlockExpression();
        } else {
            consume(TokenType.ASSIGN);
            body = parseExpression();
            consume(TokenType.SEMICOLON);
        }
        return new FunctionExpression(
                name, 
                parameters, 
                body, 
                resultType);
    }

    private Declaration finalizeClassDeclaration(boolean export) {
        var name = consume(TokenType.IDENTIFIER);
        consume(TokenType.ASSIGN);
        consume(TokenType.LBRACE);
        List<Declaration> members = new ArrayList<>();
        ClassConstructor constructor = null;
        boolean prevInClass = inClass;
        inClass = true;
        while(!match(TokenType.RBRACE)) {
            int line = peek().line();
            ConstructorOrDeclaration declaration = parseDeclarationOrConstructor(name.lexeme());
            if(declaration instanceof ClassConstructor c) {
                if(constructor != null) {
                    throw new ParseError("Already had an Constructor for class "+name.lexeme(), previous().line());
                }
                constructor = c;
            } else if(declaration instanceof Declaration d) {
                members.add(d);
            } else {
                throw new ParseError("Invalid Constructor type for Class Declaration: " + declaration.getClass(), 
                        line);
            }
        }
        inClass = prevInClass;
        consume(TokenType.SEMICOLON);
        return new ClassDeclaration(
                name, 
                members, 
                constructor, 
                export);
    }

    private ParameterInformation parseParameters() {
        List<Token> parameters = new ArrayList<>();
        List<Type> types = new ArrayList<>();
        Map<String, Type> named = new HashMap<>();
        List<OptionalParam> optional = new ArrayList<>();
        Map<String, OptionalParam> optionalNamed = new HashMap<>();
        Token varargsName = null; Type varargsType = null;
        if(peek().type() != TokenType.RPAREN) {
            do {
                if(peek().type() == TokenType.LBRACE || peek().type() == TokenType.LBRACKET)  {
                    break; // end positional parameters and parse named parameters
                }               
                parameters.add(consume(TokenType.IDENTIFIER));
                if(match(TokenType.DOTS)) {
                    varargsName = parameters.remove(parameters.size() - 1);
                    consume(TokenType.COLON);
                    varargsType = parseType();
                    break;
                }
                consume(TokenType.COLON);
                types.add(parseType());
            } while(match(TokenType.COMMA));
        }
        if(match(TokenType.LBRACKET)) { // Optional Params
            if(!match(TokenType.RBRACKET)) {
                do {
                    var name = consume(TokenType.IDENTIFIER);
                    consume(TokenType.COLON);
                    var type = parseType();
                    consume(TokenType.ASSIGN);
                    var defaultValue = parseExpression();
                    optional.add(new OptionalParam(name.lexeme(), type, defaultValue));
                } while(match(TokenType.COMMA));
                consume(TokenType.RBRACKET);
            }
            match(TokenType.COMMA); // If there is a comma, we are probably gonna see named params
        }
        if(match(TokenType.LBRACE)) { // Named Parameters
            if(!match(TokenType.RBRACE)) { // If there are any
                do {
                    var name = consume(TokenType.IDENTIFIER);
                    consume(TokenType.COLON);
                    var type = parseType();
                    if(match(TokenType.ASSIGN)) {
                        var defaultValue = parseExpression();
                        optionalNamed.put(name.lexeme(), new OptionalParam(name.lexeme(), type, defaultValue));
                    } else {
                        named.put(name.lexeme(), type);
                    }
                } while(match(TokenType.COMMA));
                consume(TokenType.RBRACE);
            }
        }
    
        return new ParameterInformation(
                parameters, 
                types,
                optional,
                named,
                optionalNamed,
                varargsName, 
                varargsType);
    }


    private Statement makeAssigment(Expression leftHandSide, Expression rightHandSide) {
        if(leftHandSide instanceof Identifier ident) {
            return new SetStatement(ident.value(), rightHandSide);
        } else if(leftHandSide instanceof IndexExpression index) {
            return new SetIndexStatement(
                    index.list(), 
                    index.index(), 
                    rightHandSide);
        } else if(leftHandSide instanceof PropertyExpression property) {
            return new SetPropertyStatement(
                    property.object(), 
                    property.name(), 
                    rightHandSide);
        } else {
            throw new ParseError("Expected Identifier as Assigment target", -1);
        }
    }

    private Expression parseExpression() {
        return booleanCombination();
    }

    private MyLangPath parsePath() {
        List<Token> names = new ArrayList<>();
        names.add(consume(TokenType.IDENTIFIER));
        while (peekNext().type() == TokenType.IDENTIFIER) {
            consume(TokenType.DOT);
            names.add(consume(TokenType.IDENTIFIER));
        }    
        return new MyLangPath(names);
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
        while(match(TokenType.STAR) || match(TokenType.SLASH) || match(TokenType.PERCENT) || match(TokenType.EXPO)) {
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
                var property = consume(TokenType.IDENTIFIER);
                left = new PropertyExpression(left, property);
            } else if(operator.type() == TokenType.LPAREN) {
                left = finalizeFunctionCall(left);
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

    private Expression finalizeFunctionCall(Expression left) {
        var parameters = parseCommaSeparated(TokenType.RPAREN);
        var positional = parameters.stream()
            .filter(p -> !(p instanceof NamedParameter))
            .toList();
        var named = parameters.stream()
            .filter(p -> p instanceof NamedParameter)
            .map(p -> (NamedParameter) p)
            .collect(Collectors.toMap(p -> p.name().lexeme(), p -> p.parameter()));
        return new FunctionCall(left, positional, named);
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
                var body = parseParameter();
                return new WhileYieldExpression(condition, body);
            }
        } else if(match(TokenType.FOR)) {
            var variableName = consume(TokenType.IDENTIFIER);
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
                var body = parseParameter();
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
        if(match(TokenType.IDENTIFIER)) {
            return new Identifier(previous());
        } else if(match(TokenType.NEW)) {
            var className = consume(TokenType.IDENTIFIER);
            Expression classAccess = new Identifier(className);
            while(match(TokenType.DOT)) {
                var subName = consume(TokenType.IDENTIFIER);
                classAccess = new PropertyExpression(classAccess, subName);
            }
            consume(TokenType.LPAREN);
            return finalizeFunctionCall(classAccess);
        } else if(match(TokenType.VALUE_THIS)) {
            return new ThisExpression(previous());
        } else {
            return literal();
        }
    }

    private Statement parseStatementOrExpression() {
        if(match(TokenType.SEMICOLON)) return new EmptyStatement(previous());
        var start = current;
        if(match(TokenType.IF)) {
            var condition = parseExpression();
            if(match(TokenType.DO)) {
                var body = parseStatementOrExpression();
                return new IfStatement(condition, body);
            } else {
                current = start;
            }
        } else if(match(TokenType.RETURN)) {
            var body = parseExpression();
            return new ReturnStatement(body);
        }
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

    private Parameter parseParameter() {
        if(peek().type() == TokenType.IDENTIFIER && peekNext().type() == TokenType.COLON) {
            var name = consume(TokenType.IDENTIFIER);
            consume(TokenType.COLON);
            var value = parseExpression();
            return new NamedParameter(name, value);
        }
        var expr = parseExpression();
        if(match(TokenType.DOTS)) {
            return new SpreadParameter(expr);
        } else if(match(TokenType.IF)) {
            var guard = parseExpression();
            return new ConditionalParameter(expr, guard);
        } else {
            return new ExpressionParameter(expr);
        }
    }

    private Expression finishListExpression() {
        if(match(TokenType.RBRACKET)) {
            return new ListExpression(List.of());
        }
        var first = parseParameter();
        if(previous().type() == TokenType.DOTS) {
            if(!match(TokenType.COMMA)) {
                if(match(TokenType.RBRACKET)) {
                    var list = List.of(first);
                    verifyNoNamedParameters(list);
                    return new ListExpression(list);
                }
                var second = parseExpression();
                Expression step = new NumericLiteral(1);
                if(match(TokenType.COLON)) {
                    step = parseExpression();
                }
                consume(TokenType.RBRACKET);
                return new RangeExpression(((SpreadParameter) first).collection(), second, step);
            } else {
                var next = parseCommaSeparated(TokenType.RBRACKET);
                next.add(0, first);
                verifyNoNamedParameters(next);
                return new ListExpression(next);
            }
        } else {
            if(match(TokenType.RBRACKET)) {
                var list = List.of(first);
                verifyNoNamedParameters(list);
                return new ListExpression(list);
            }
            consume(TokenType.COMMA);
            var next = parseCommaSeparated(TokenType.RBRACKET);
            next.add(0, first);
            verifyNoNamedParameters(next);
            return new ListExpression(next);
        }
    }

    private void verifyNoNamedParameters(List<Parameter> parameters) {
        if(parameters.stream().anyMatch(p -> p instanceof NamedParameter)) {
            throw new ParseError("Named Parameters are not allowed in lists", previous().line());
        }
    }

    private Expression finishFunctionExpression() {
        consume(TokenType.LPAREN);
        var parameters = parseParameters();
        consume(TokenType.RPAREN);
        consume(TokenType.COLON);
        var returnType = parseType();
        Expression body;
        if(peek().type() == TokenType.LPAREN) {
            body = finishFunctionExpression();
        } else {
            if(match(TokenType.LBRACE)) {
                body = finishBlockExpression();
            } else {
                consume(TokenType.ASSIGN);
                body = parseExpression();
            }
        }
        return new FunctionExpression("Anonymous Function", parameters, body, returnType);
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

    private List<Parameter> parseCommaSeparated(TokenType endDelimiter) {
        List<Parameter> parameters = new ArrayList<>();
        if(!match(endDelimiter)) {
            do {
                parameters.add(parseParameter());
            } while(match(TokenType.COMMA));
            consume(endDelimiter);
        }
        return parameters;
    }

    private Expression literal() {
        if(match(TokenType.NUMBER_LITERAL)) {
            Token literal = previous();
            Expression left = new NumericLiteral(Double.parseDouble(literal.lexeme()));
            if(peek().type() == TokenType.IDENTIFIER) {
                Expression multiplied = propertyOrCall();
                return new BinaryOperation(new Token(TokenType.STAR, literal.lexeme(),literal.line()), left, multiplied);
            } else if(match(TokenType.LPAREN)) {
                Expression result = left;
                do {
                    Token op = previous();
                    Expression multiplied = parseExpression();
                    consume(TokenType.RPAREN);
                    result = new BinaryOperation(new Token(TokenType.STAR, op.lexeme(), op.line()), result, multiplied);
                } while(match(TokenType.LPAREN));
                return result;
            }
            return left;
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




