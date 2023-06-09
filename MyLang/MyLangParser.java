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

    private PrettyPrinter p = new PrettyPrinter();

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
            throw new ParseError("Expected " + type + ", got " + peek().type() + (peek().type() != TokenType.ERROR ? "" : peek()), peek().line());
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
                (peek().type().startsDeclaration() && 
                    !(peek().type() == TokenType.FUN && peekNext().type() != TokenType.IDENTIFIER))) {
            return parseDeclaration();
        } else if(match(TokenType.SEMICOLON)) {
            return new EmptyDeclaration(previous());
        } else {
            var start = current;
            if(match(TokenType.IF)) { // Try Parsing an If Statement. Backtrack if If Expression
                if(match(TokenType.VAL)) {
                    current = start;
                } else {
                var condition = parseExpression();
                if(match(TokenType.DO)) {
                    var body = finishBlockExpression();
                    return new IfStatement(condition, new ExpressionStatement(body));
                } else { // Backtrack and instead parse a expression
                    current = start;
                }}
            } else if(match(TokenType.FOR)) { // Try Parsing an For Statement. Backtrack if For Expression
                var pattern = parsePattern();
                consume(TokenType.IN);
                var collection = parseExpression();
                Expression guard = new BooleanLiteral(true);
                if(match(TokenType.IF)) {
                    guard = parseExpression();
                }
                if(match(TokenType.DO)) {
                    var body = finishBlockExpression();
                    return new ForDoStatement(pattern, collection, guard, body);
                } else {
                    current = start;
                }
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
        if(declarationType.type() == TokenType.NEW) {
            return finalizeClassConstructor(typeName);
        } else {
            throw new ParseError("Invalid constructor declaration start: "+declarationType.type(), 
                    declarationType.line());
        }
    }

    private ClassConstructor finalizeClassConstructor(String typeName) {
        var keyword = previous();
        consume(TokenType.LPAREN);
        var parameters = parseParameters(false);
        consume(TokenType.RPAREN);
        consume(TokenType.DO);
        var body = finishBlockExpression();
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
            } else if(match(TokenType.LPAREN)) {
                List<Type> args = new ArrayList<>();
                if(!match(TokenType.RPAREN)) {
                    do {
                        args.add(parseType());
                    } while(match(TokenType.COMMA));
                    consume(TokenType.RPAREN);
                }
                left = new TypeAppl(left, args);
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
            return tupleType();
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
            Type returnType = new VoidType();
            if(match(TokenType.COLON)) {
                returnType = parseType();
            }
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

    private Type tupleType() {
        List<Type> elements = new ArrayList<>();
        if(!match(TokenType.RPAREN)) {
            do {
                elements.add(parseType());
            } while(match(TokenType.COMMA));
            consume(TokenType.RPAREN);
        }
        if(elements.size() == 1) {
            return elements.get(0);
        }
        return new Tuple(elements);
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
            case ENUM -> finalizeEnumDeclaration(export);
            case TYPE -> finalizeTypeDefDeclaration(export);
            case SEMICOLON -> new EmptyDeclaration(declarationType);
            default -> throw new ParseError("Unknown declaration type: " + declarationType.type(), 
                    declarationType.line());
        };
    }
    private Declaration finalizeEnumDeclaration(boolean export) {
        var name = consume(TokenType.IDENTIFIER);
        List<Token> args = null;
        if(match(TokenType.LPAREN)) {
            args = new ArrayList<>();
            if(!match(TokenType.RPAREN)) {
                do {
                    args.add(consume(TokenType.IDENTIFIER));
                } while(match(TokenType.COMMA));
                consume(TokenType.RPAREN);
            }
        }
        consume(TokenType.WHERE);
        List<EnumConstructor> variants = new ArrayList<>();
        List<FunctionDeclaration> methods = new ArrayList<>();
        if(peek().type() == TokenType.IDENTIFIER) {
            while(true) {
                variants.add(parseEnumVariant());
                if(match(TokenType.SEMICOLON)) {
                    break;
                } else {
                    consume(TokenType.COMMA);
                }
            }
        }
        while(!match(TokenType.END)) {
            consume(TokenType.FUN);
            methods.add(finalizeFunctionDeclaration(true));
        }
        var result = new EnumDeclaration(name, args, variants, export, methods);
        return result;
    }

    private EnumConstructor parseEnumVariant() {
        var name = consume(TokenType.IDENTIFIER);
        consume(TokenType.LPAREN);
        List<Type> params = new ArrayList<>();
        if(!match(TokenType.RPAREN)) {
            do {
                params.add(parseType());
            } while(match(TokenType.COMMA));
            consume(TokenType.RPAREN);
        }
        return new EnumConstructor(name, params);
    }

    private Declaration finalizeTypeDefDeclaration(boolean export) {
        var name = consume(TokenType.IDENTIFIER);
        List<Token> args = null;
        if(match(TokenType.LPAREN)) {
            args = new ArrayList<>();
            if(!match(TokenType.RPAREN)) {
                do {
                    args.add(consume(TokenType.IDENTIFIER));
                } while(match(TokenType.COMMA));
                consume(TokenType.RPAREN);
            }
        }
        consume(TokenType.ASSIGN);
        var def = parseType();
        consume(TokenType.SEMICOLON);
        return new TypeDefDeclaration(name, args, def, export);
    }

    private Import finalizeImportDeclaration() {
        var name = parsePath();
        consume(TokenType.SEMICOLON);
        return new ImportDeclaration(name);
    }

    private Declaration finalizeVariableDeclaration(boolean isReassignable, boolean export) {
        var pat = parsePattern();
        Type type = null;
        if(match(TokenType.COLON)) {
            type = parseType();
        }
        Expression initializer;
        if(inClass && match(TokenType.SEMICOLON)) {
            initializer = new NullLiteral(previous());
        } else {
            consume(TokenType.ASSIGN);
            initializer = parseExpression();
            if(match(TokenType.ELSE)) {
                var elseBranch = parseExpression();
                consume(TokenType.SEMICOLON);
                if(type != null) {
                    throw new ParseError("Type Annotations are not allowed in val-else", previous().line());
                }
                if(isReassignable) {
                    throw new ParseError("Val-Else is not allowed to be reassigneable, use val instead of var", 
                        previous().line());
                }
                return new ValElseDeclaration(pat, initializer, elseBranch);
            }
            consume(TokenType.SEMICOLON);
        }

        return new VariableDeclaration(
                pat,
                type, 
                initializer, 
                isReassignable, 
                export);
    }
    private FunctionDeclaration finalizeFunctionDeclaration(boolean export) {
        var name = consume(TokenType.IDENTIFIER);
        List<Token> typeParams = new ArrayList<>();
        if(match(TokenType.LBRACKET)) {
            if(!match(TokenType.RBRACKET)) {
                do {
                    typeParams.add(consume(TokenType.IDENTIFIER));
                } while(match(TokenType.COMMA));
                consume(TokenType.RBRACKET);
            }
        }
        var expression = finalizeFunctionExpressionWithName(name.lexeme(), 1);
        return new FunctionDeclaration(
                name, 
                typeParams,
                expression.parameters(), 
                expression.body(), 
                expression.retType(), 
                export);
    }
    private FunctionExpression finalizeFunctionExpressionWithName(String name, int counter) {
        consume(TokenType.LPAREN);
        var parameters = parseParameters(false);
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
        if(match(TokenType.DO)) {
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
        List<Token> args = null;
        if(match(TokenType.LPAREN)) {
            args = new ArrayList<>();
            if(!match(TokenType.RPAREN)) {
                do {
                    args.add(consume(TokenType.IDENTIFIER));
                } while(match(TokenType.COMMA));
                consume(TokenType.RPAREN);
            }
        }
        consume(TokenType.WHERE);
        List<Declaration> members = new ArrayList<>();
        ClassConstructor constructor = null;
        boolean prevInClass = inClass;
        inClass = true;
        while(!match(TokenType.END)) {
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
        return new ClassDeclaration(
                name, 
                args,
                members, 
                constructor, 
                export);
    }

    private ParameterInformation parseParameters(boolean allowInferredTypes) {
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
                if(!allowInferredTypes) {
                    consume(TokenType.COLON);
                    types.add(parseType());
                } else {
                    if(match(TokenType.COLON)) {
                        types.add(parseType());
                    } else {
                        types.add(null);
                    }
                }
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
        return new SetStatement(makeSetter(leftHandSide), rightHandSide);
    }
    private Setter makeSetter(Expression from) {
        if(from instanceof Identifier i) {
            return new VariableSetter(i.value());
        } else if(from instanceof IndexExpression i) {
            return new IndexSetter(i.list(), i.index());
        } else if(from instanceof PropertyExpression p) {
            return new PropertySetter(p.object(), p.name());
        } else if(from instanceof WildcardExpression w) {
            return new WildcardSetter();
        } else if(from instanceof TupleExpression t) {
            return new TupleSetter(t.elements().stream().map(this::makeSetter).toList());
        } else {
            throw new ParseError("Invalid Setter: "+p.prettyPrint(from), previous().line());
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
                if(match(TokenType.LBRACKET)) { // Instantiation
                    List<Type> args = new ArrayList<>();
                    if(!match(TokenType.RBRACKET)) {
                        do {
                            args.add(parseType());
                        } while(match(TokenType.COMMA));
                        consume(TokenType.RBRACKET);
                    }
                    left = new InstExpression(left, args);
                } else {
                    boolean negated = match(TokenType.BANG);
                    Token negatedOp = previous();
                    var property = consume(TokenType.IDENTIFIER);
                    left = new PropertyExpression(left, property);
                    if(negated) {
                        if(match(TokenType.LPAREN)) {
                            left = finalizeFunctionCall(left);
                        }
                        left = new UnaryOperation(negatedOp, left);
                    }
                }
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
            return tupleExpression();
        } else if(match(TokenType.IF)) {
            if(match(TokenType.VAL)) {
                var pat = parsePattern();
                consume(TokenType.ASSIGN);
                var matched = parseExpression();
                consume(TokenType.THEN);
                var thenBranch = parseExpression();
                consume(TokenType.ELSE);
                var elseBranch = parseExpression();
                return new IfValExpression(pat, matched, thenBranch, elseBranch);
            }
            var condition = parseExpression();
            consume(TokenType.THEN);
            var thenBranch = parseExpression();
            consume(TokenType.ELSE);
            var elseBranch = parseExpression();
            return new IfExpression(condition, thenBranch, elseBranch);
        } else if(match(TokenType.WHILE)) {
            return finishWhileExpression();
        } else if(match(TokenType.FOR)) {
            var pat = parsePattern();
            consume(TokenType.IN);
            var collection = parseExpression();
            Expression guard = new BooleanLiteral(true);
            if(match(TokenType.IF)) {
                guard = parseExpression();
            }
            consume(TokenType.YIELD);
            var body = finishBlockExpression();
            return new ForYieldExpression(pat, collection, guard, new ExpressionParameter(body));
        } else if(match(TokenType.DO)) {
            return finishBlockExpression();
        } else if(match(TokenType.LBRACKET)) {
            return finishListExpression();
        } else if(match(TokenType.FUN)) {
            return finishFunctionExpression();
        } else if(match(TokenType.MATCH)) {
            return finishMatchExpression();
        } else if(match(TokenType.RETURN)) {
            var value = parseExpression();
            return new ReturnExpression(value);
        }
        return someIdentifierOrNew();
    }

    private Expression tupleExpression() {
        List<Expression> elements = new ArrayList<>();
        if(!match(TokenType.RPAREN)) {
            do {
                elements.add(parseExpression());
            } while(match(TokenType.COMMA));
            consume(TokenType.RPAREN);
        }
        if(elements.size() == 1) {
            return elements.get(0);
        }
        return new TupleExpression(elements);
    }

    private Expression finishWhileExpression() {
        if(match(TokenType.VAL)) {
            var pat = parsePattern();
            consume(TokenType.ASSIGN);
            var matched = parseExpression();
            if(match(TokenType.DO)) {
                var body = finishBlockExpression();
                return new WhileValDoExpression(pat, matched, body);
            } else {
                consume(TokenType.YIELD);
                var body = finishBlockExpression();
                return new WhileValYieldExpression(pat, matched, body);
            }
        } else {
            var condition = parseExpression();
            if(match(TokenType.DO)) {
                var body = finishBlockExpression();
                return new WhileDoExpression(condition, new ExpressionStatement(body));
            } else {
                consume(TokenType.YIELD);
                var body = finishBlockExpression();
                return new WhileYieldExpression(condition, new ExpressionParameter(body));
            }
        }
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
        var parameters = parseParameters(true);
        consume(TokenType.RPAREN);
        Type returnType = null;
        if(match(TokenType.COLON)) {
            returnType = parseType();
        }
        Expression body;
        if(peek().type() == TokenType.LPAREN) {
            body = finishFunctionExpression();
        } else {
            if(match(TokenType.DO)) {
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
        while(!match(TokenType.END)) {
            var next = parseAny();
            if(next instanceof Expression finalExpression) {
                consume(TokenType.END);
                return new BlockExpression(statements, finalExpression);
            } else {
                statements.add((DeclarationOrStatement) next);
            }
        }

        return new BlockExpression(statements, new NullLiteral(previous()));
    }

    private Expression finishMatchExpression() {
        var matched = parseExpression();
        consume(TokenType.DO);
        List<Pattern> pats = new ArrayList<>();
        List<Expression> branches = new ArrayList<>();
        while(!match(TokenType.END)) {
            consume(TokenType.CASE);
            var pat = parsePattern();
            consume(TokenType.ASSIGN);
            var expr = parseExpression();
            consume(TokenType.SEMICOLON);
            pats.add(pat);
            branches.add(expr);
        }
        return new MatchExpression(
            matched,
            pats,
            branches
        );
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
        } else if(match(TokenType.QUESTION_MARK)) {
            return new WildcardExpression(previous());
        } else {
            throw new ParseError("Expected literal, got "+next().type(), next().line());
        }
    }

    private Pattern parsePattern() {
        if(match(TokenType.QUESTION_MARK)) {
            return new Wildcard();
        } else if(match(TokenType.NUMBER_LITERAL)) {
            return new NumberPattern(Double.parseDouble(previous().lexeme()));
        } else if(match(TokenType.STRING_LITERAL)) {
            return new StringPattern(previous().lexeme());
        } else if(match(TokenType.TRUE)) {
            return new BooleanPattern(true);
        } else if(match(TokenType.FALSE)) {
            return new BooleanPattern(false);
        } else if(match(TokenType.IDENTIFIER)) {
            var name = previous();
            if(!match(TokenType.LPAREN)) {
                return new VariableBinding(name);
            }
            List<Pattern> subPatterns = new ArrayList<>();
            if(!match(TokenType.RPAREN)) {
                do {
                    subPatterns.add(parsePattern());
                } while(match(TokenType.COMMA));
                consume(TokenType.RPAREN);
            }
            return new ConstructorPattern(name, subPatterns);
        } else if(match(TokenType.LPAREN)) {
            List<Pattern> subPatterns = new ArrayList<>();
            if(!match(TokenType.RPAREN)) {
                do {
                    subPatterns.add(parsePattern());
                } while(match(TokenType.COMMA));
                consume(TokenType.RPAREN);
            }
            if(subPatterns.size() == 1) {
                return subPatterns.get(0);
            }
            return new TuplePattern(subPatterns);
        }
        throw new ParseError("Invalid Pattern start: "+peek().type(), peek().line());
    }

}




