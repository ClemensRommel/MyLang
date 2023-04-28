package MyLang;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import static MyLang.MyLangAST.*;


public class Typechecker implements 
        DeclarationVisitor<Void>, ExpressionVisitor<Void>, StatementVisitor<Void> {
    private boolean hadError = false;
    private List<String> errors = new ArrayList<>();

    TypeEnv env = new TypeEnv();
    PrettyPrinter p = new PrettyPrinter();
    SubtypeChecker s = new SubtypeChecker();
    TypeInferrer ti = new TypeInferrer(this);
    TypeCompiler tcomp = new TypeCompiler(this);

    private TypeRep checkTarget = null;
    TypeRep currentReturnType = null;

    MyLangRunner runner;

    private String currentFileName = null;

    private boolean inConstructor = false;
    private boolean inClass = false;

    public Typechecker(MyLangRunner runner) {
        this.runner = runner;

        declareBuiltins();
    }

    public static boolean typechecks(MyLangRunner runner,MyLangFile program, String name) {
        Typechecker tc = new Typechecker(runner);
        tc.currentFileName = runner.unresolve(name);
        tc.gatherImports(program.imports());
        tc.gatherTypes(program.declarations());


        for(Declaration decl: program.declarations()) {
            tc.checkDeclaration(decl);
        }

        runner.typecheckedFiles.put(name, new MyLangAST.Module(tc.currentFileName, tc.env));

        if(tc.hadError) {
            for(var error: tc.errors) {
                System.out.println(error);
            }
            return false;
        } else {
            return true;
        }
    }

    public void declareBuiltins() {
        for(var builtin: MyLangInterpreter.builtins) {
            declareType(builtin.getName(), builtin.type, false);
        }
    }

    private void gatherImports(List<Import> imports) {
        for(Import i: imports) {
            declareImport(i);
        }
    }

    private void declareImport(Import i) {
        var v = (ImportDeclaration) i;
        
        declareModule(importPath(v.Name()), v.Name());
    }
    private MyLangAST.Module importPath(MyLangPath path) {
        return runner.typecheckedFiles.get(runner.resolvePath(path));
    }

    private void declareModule(MyLangAST.Module module, MyLangPath path) {
        env.declareModule(module, path);
    }

    private void checkType(TypeRep target, Expression tested) {
        TypeRep previousTarget = checkTarget;
        checkTarget = target;

        tested.accept(this);

        checkTarget = previousTarget;
    }

    private TypeRep inferType(Expression e) {
        var resultType = ti.infer(e);
        checkType(resultType, e);
        if(resultType instanceof UnknownType) {
            error("Could not infer type of expression "+p.prettyPrint(e));
        }
        //System.out.println("inferred type "+showType(resultType)+" of expression "+e);
        return env.normalize(resultType, this);
    }

    TypeRep inferElemTypeOfParameter(Parameter param, boolean inferMode) {
        if(param instanceof ExpressionParameter e) {
            return inferType(e.expr());
        } else if(param instanceof ConditionalParameter c) {
            checkType(booleanType, c.guard());
            return inferType(c.body());
        } else if(param instanceof SpreadParameter s) {
            var collectionType = inferType(s.collection());
            if(collectionType instanceof ListOfRep l) {
                return l.elements();
            } else {
                if(!inferMode) error("Expected list, got a '"+showType(collectionType)+"'");
                return unknown();
            }
        } else {
            error("Could not infer type of Parameter "+p.prettyPrint(param));
            return unknown();
        }
    }

    void openScope() {
        env = env.openScope();
    }
    void closeScope() {
        env = env.closeScope();
    }

    void error(String message) {
        hadError = true;
        errors.add(currentFileName+": "+message);
    }

    private void checkConstructor(ClassConstructor c) {
        openScope();
        boolean prevInConstructor = inConstructor;
        inConstructor = true;
        for(int i = 0; i < c.parameters().names().size(); i++) {
            declareType(
                    c.parameters().names().get(i).lexeme(), 
                    tcomp.compileType(c.parameters().types().get(i)), 
                    false);
        }
        if(c.parameters().varargsName() != null) {
            declareType(
                    c.parameters().varargsName().lexeme(), 
                    tcomp.compileType(c.parameters().varargsType()), 
                    false);
        }
        c.parameters().optionals().forEach(optional -> {
            var type = tcomp.compileType(optional.type());
            checkType(type, optional.defaultValue());
            declareType(optional.name(), type, false);
        });
        c.parameters().named().forEach((var name, var type) -> {
            declareType(name, tcomp.compileType(type), false);
        });
        c.parameters().optionalNamed().forEach((var name, var type) -> {
            declareType(name, tcomp.compileType(type.type()), false);
        });
        var previousReturnType = currentReturnType;
        currentReturnType = voidType;
        checkType(voidType, c.body());
        currentReturnType = previousReturnType;
        inConstructor = prevInConstructor;
        closeScope();
    }

    private void checkDeclaration(Declaration d) {
        d.accept(this);
    }
    private void checkStatement(Statement s) {
        s.accept(this);
    }

    private void checkDeclOrStatement(DeclarationOrStatement ds) {
        if(ds instanceof Declaration d) {
            checkDeclaration(d);
        } else if(ds instanceof Statement s) {
            checkStatement(s);
        }
    }

    void gatherTypes(List<Declaration> decls) {
        for(Declaration decl: decls) {
            declare(decl);
        }
    }

    private void declareNewType(String name, TypeRep type) {
        env.addType(name, type);
    }

    void declare(DeclarationOrStatement decl) {
        if(decl instanceof VariableDeclaration v) {
            declareType(v.Name().lexeme(), tcomp.compileType(v.type()), v.isReassignable());
            if(v.export()) {
                env.exportValue(v.Name().lexeme());
            }
        } else if(decl instanceof FunctionDeclaration f) {
            declareType(f.Name().lexeme(), functionTypeOf(f), false);
            if(f.export()) {
                env.exportValue(f.Name().lexeme());
            }
        } else if(decl instanceof ClassDeclaration c) {
            declareNewType(c.Name().lexeme(), classTypeOf(c));
            declareType(c.Name().lexeme(), constructorTypeOf(c), false);
            if(c.export()) {
                env.exportValue(c.Name().lexeme());
                env.exportType(c.Name().lexeme());
            }
        } else if(decl instanceof TypeDefDeclaration t) {
            var definition = tcomp.compileType(t.definition());
            declareNewType(t.Name().lexeme(), definition);
            if(t.export()) {
                env.exportType(t.Name().lexeme());
            }
        } else if(decl instanceof EnumDeclaration e) {
            declareNewType(e.Name().lexeme(), enumTypeOf(e));
            for(var constructor: e.variants()) {
                declareType(constructor.name().lexeme(),
                    enumConstructorTypeOf(constructor, e.Name()), false);
                if(e.export()) {
                    env.exportValue(constructor.name().lexeme());
                }
            }
            if(e.export()) {
                env.exportType(e.Name().lexeme());
            }
        }
    }
    TypeRep enumTypeOf(EnumDeclaration e) {
        return new EnumType(e.Name(), 
            e.variants().stream().collect(Collectors.toMap(
                c -> c.name().lexeme(), 
                c -> enumConstructorTypeOf(c, e.Name())
                )
            ),
            env);
    }
    TypeRep enumConstructorTypeOf(EnumConstructor e, Token enumName) {
        return new FunctionTypeRep(
            e.parameters().stream().map(tcomp::compileType).toList(),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            new TypeIdentifierRep(enumName, env),
            env
        );
    }

    TypeRep functionTypeOf(FunctionDeclaration f) {
        return new FunctionTypeRep(
                f.parameters().types().stream().map(tcomp::compileType).toList(),
                f.parameters().optionals().stream().map(OptionalParam::type).map(tcomp::compileType).toList(),
                tcomp.namedTypesIn(f.parameters().named()),
                tcomp.optionalNamedTypesIn(f.parameters().optionalNamed()),
                tcomp.compileType(f.parameters().varargsType()),
                tcomp.compileType(f.retType()),
                env);
    }

    TypeRep classTypeOf(ClassDeclaration c) {
        return new ClassType(
                    c.Name(),
                    accessorsIn(c),
                    getReadability(c.fieldsAndMethods()),
                    constructorTypeOf(c),
                    env
                );
    }

    Map<String, Boolean> getReadability(List<Declaration> decls) {
        Map<String, Boolean> map = new HashMap<>();
        for(Declaration decl: decls) {
            if(decl instanceof VariableDeclaration v) {
                map.put(v.Name().lexeme(), v.isReassignable());
            } else if(decl instanceof FunctionDeclaration f) {
                map.put(f.Name().lexeme(),false);
            }
        }
        return map;
    }

    Map<String, TypeRep> accessorsIn(ClassDeclaration c) {
        openScope();
        gatherTypes(c.fieldsAndMethods());
        Map<String, TypeRep> types = env.getDeclaredTypesOfValues();
        closeScope();
        return types;
    }
    FunctionTypeRep constructorTypeOf(ClassDeclaration c) {
        if(c.constructor() == null) { // Standard Constructor
            return new FunctionTypeRep(
                    List.of(),
                    List.of(),
                    Map.of(),
                    Map.of(),
                    null,
                    new TypeIdentifierRep(c.Name(), env),
                    env);
        }
        return new FunctionTypeRep(
                c.constructor().parameters().types().stream().map(tcomp::compileType).toList(),
                c.constructor().parameters().optionals().stream().map(OptionalParam::type).map(tcomp::compileType).toList(),
                tcomp.namedTypesIn(c.constructor().parameters().named()),
                tcomp.optionalNamedTypesIn(c.constructor().parameters().optionalNamed()),
                tcomp.compileType(c.constructor().parameters().varargsType()),
                new TypeIdentifierRep(c.Name(), env),
                env
                );
    }

    void declareType(String name, TypeRep type, boolean isReassignable) {
        env.addValue(name, type, isReassignable);
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclaration value) {
        if(inClass && value.initializer() instanceof NullLiteral) {
            return null;
        }
        checkType(tcomp.compileType(value.type()), value.initializer());
        return null;
    }

    @Override
    public Void visitFunctionDeclaration(FunctionDeclaration value) {
        openScope();
        for(int i = 0; i < value.parameters().names().size(); i++) {
            declareType(value.parameters().names().get(i).lexeme(), tcomp.compileType(value.parameters().types().get(i)), false);
        }
        if(value.parameters().varargsName() != null) {
            declareType(
                    value.parameters().varargsName().lexeme(), 
                    new ListOfRep(tcomp.compileType(value.parameters().varargsType())), 
                    false);
        }
        value.parameters().optionals().forEach(optional -> {
            var type = tcomp.compileType(optional.type());
            checkType(type, optional.defaultValue());
            declareType(optional.name(), type, false);
        });
        value.parameters().named().forEach((var name, var type) -> {
            declareType(name, tcomp.compileType(type), false);
        });
        value.parameters().optionalNamed().forEach((var name, var type) -> {
            declareType(name, tcomp.compileType(type.type()), false);
        });
        var retType = tcomp.compileType(value.retType());
        var previousReturnType = currentReturnType;
        currentReturnType = retType;
        checkType(retType, value.body());
        currentReturnType = previousReturnType;
        closeScope();
        return null;
    }

    @Override
    public Void visitClassDeclaration(ClassDeclaration value) {
        var classType = env.getTypeByName(value.Name().lexeme());
        openScope();
        boolean prevInClass = inClass;
        inClass = true;
        declareType("this", classType, false);
        for(Declaration declaration: value.fieldsAndMethods()) {
            checkDeclaration(declaration);
        }
        if(value.constructor() != null)  {
            checkConstructor(value.constructor());
        }

        inClass = prevInClass;

        closeScope();

        return null;
    }

    @Override
    public Void visitModuleDeclaration(ModuleDeclaration value) {
        return null;
    }

    @Override
    public Void visitEmptyDeclaration(EmptyDeclaration value) {
        return null;
    }

    String showType(TypeRep t) {
        return p.prettyPrint(t);
    }

    void typeMismatch(TypeRep expected, TypeRep given) {
        error("Expected '"+showType(expected)+"', got '"+showType(given)+"'");
    }

    boolean isAssigneableTo(TypeRep to, TypeRep from) {
        if(to == null) {
            throw new RuntimeException("to cannot be null");
        }
        if(from == null) {
            throw new RuntimeException("from cannot be null");
        }
        to = env.normalize(to, this);
        from = env.normalize(from, this);
        if(to.equals(unknown()) || from.equals(unknown())) {
            hadError = true;
            return true;
        }
        if(to.equals(voidType)) {
            return true;
        }
        return s.isSubtypeOf(to, from);
    }

    boolean exists(String name) {
        return env.valueExists(name);
    }

    static TypeRep numberType = new Builtin(BuiltinType.NUMBER);

    @Override
    public Void visitNumericLiteral(NumericLiteral value) {
        hasType(numberType);
        return null;
    }

    static TypeRep stringType = new Builtin(BuiltinType.STRING);

    @Override
    public Void visitStringLiteral(StringLiteral value) {
        hasType(stringType);
        return null;
    }

    static TypeRep booleanType = new Builtin(BuiltinType.BOOLEAN);

    @Override
    public Void visitBooleanLiteral(BooleanLiteral value) {
        hasType(booleanType);
        return null;
    }

    static TypeRep voidType = new Builtin(BuiltinType.VOID);

    @Override
    public Void visitNullLiteral(NullLiteral value) {
        hasType(voidType);
        return null;
    }

    void unknownVariable(Token name) {
        error("["+name.line()+"] Unknown variable '"+name.lexeme()+"'");
    }

    static TypeRep unknown() {return new UnknownType();}

    TypeRep getTypeOf(Token name) {
        return getTypeOf(name, false);
    }
    TypeRep getTypeOf(Token name, boolean inferMode) {
        var result = env.getTypeOfValue(name.lexeme());
        if(result instanceof UnknownType && !inferMode) {
            unknownVariable(name);
        }
        return result;
    }

    boolean isReassignable(String name) {
        return env.isReassigneable(name);
    }

    @Override
    public Void visitIdentifier(Identifier value) {
        if(!exists(value.value().lexeme())) {
            unknownVariable(value.value());
        } else {
            hasType(getTypeOf(value.value()));
        }
        return null;
    }

    @Override 
    public Void visitBinaryOperation(BinaryOperation b) {
        switch(b.operator().type()) {
            case AND, OR, XOR -> {
                checkType(booleanType, b.left());
                checkType(booleanType, b.right());

                hasType(booleanType);
            }
            case PLUS -> {
                var leftType = inferType(b.left());
                if(leftType.equals(stringType)) {
                    checkType(voidType, b.right());
                    hasType(stringType);
                } else {
                    var rightType = inferType(b.right());
                    if(rightType.equals(stringType)) {
                        hasType(stringType);
                    } else if(leftType.equals(numberType) && rightType.equals(numberType)) {
                        hasType(numberType);
                    } else {
                        error("["+b.operator().line()+"] Expected Number or String, got "+showType(leftType));
                        error("["+b.operator().line()+"] Expected Number or String, got "+showType(rightType));
                    }
                }
            }
            case MINUS, STAR, SLASH, PERCENT, EXPO -> {
                checkType(numberType, b.left());
                checkType(numberType, b.right());

                hasType(numberType);
            }
            case GREATER, GREATER_EQUAL, LESS, LESS_EQUAL -> {
                checkType(numberType, b.left());
                checkType(numberType, b.right());

                hasType(booleanType);
            }
            case EQUAL -> {
                var leftType = inferType(b.left());
                checkType(leftType, b.right());

                hasType(booleanType);
            }
            case IN -> {
                TypeRep elementType = inferType(b.left());
                checkType(new ListOfRep(elementType), b.right());

                hasType(booleanType);
            }
            default -> throw new RuntimeException("Unknown binary operator: "+b.operator().lexeme());
        }

        return null;
    }

    @Override
    public Void visitUnaryOperation(UnaryOperation o) {
        if(o.operator().type() == TokenType.BANG) {
            checkType(booleanType, o.operand());
            hasType(booleanType);
        } else if(o.operator().type() == TokenType.PLUS || o.operator().type() == TokenType.MINUS) {
            checkType(numberType, o.operand());
            hasType(numberType);
        }

        return null;
    }
    
    void noFunctionType(TypeRep t, Expression e) {
        error("Cannot call value of type '"+showType(t)+"' in expression "+p.prettyPrint(e));
    }

    void checkParameter(TypeRep needed, Parameter p, boolean inVarargs) {
        if(!inVarargs) {
            if(p instanceof ExpressionParameter e) {
                checkType(needed, e.expr());
            } else {
                error("Can only use spread operator or conditional parameter in varargs or lists");
            }
        } else {
            if(p instanceof ExpressionParameter e) {
                checkType(needed, e.expr());
            } else if(p instanceof SpreadParameter s) {
                checkType(new ListOfRep(needed), s.collection());
            } else if(p instanceof ConditionalParameter c) {
                checkType(booleanType, c.guard());
                checkType(needed, c.body());
            }
        }
    }

    void checkFunctionType(FunctionTypeRep t, FunctionCall c) {
        if(c.arguments().size() != t.parameters().size()) {
            if(c.arguments().size() < t.parameters().size()
                    || t.varargsType() == null ) {
                error("Not enough arguments: got "+c.arguments().size()+
                        ", expected "+t.parameters().size()+
                        " (type: "+showType(t)+")");
                return;
            }
        }
        for(int i = 0; i < t.parameters().size(); i++) {
            checkParameter(
                    env.normalize(t.parameters().get(i), this), 
                    c.arguments().get(i), false);
        }
        if(t.varargsType() != null) {
            for(int i = t.parameters().size(); i < c.arguments().size(); i++) {
                checkParameter(env.normalize(t.varargsType(), this), c.arguments().get(i), true);
            }
        }
        for(var namedParam: t.named().entrySet()) { // All required arguments are given
            if(!c.named().containsKey(namedParam.getKey())) {
                error("Call to Function "+p.prettyPrint(c.callee())+
                    " does not have required named Parameter '"+namedParam.getKey()+
                    "' of Type "+ showType(namedParam.getValue()));
            } else {
                checkType(namedParam.getValue(), c.named().get(namedParam.getKey()));
            }
        }
        for(var namedParam: c.named().entrySet()) { // All given named params are given
            if(!t.named().containsKey(namedParam.getKey()) && !t.optionalNamed().containsKey(namedParam.getKey())) {
                error("Function '"+p.prettyPrint(c.callee())+" does not have named parameter '"+namedParam.getKey()+"'");
            }
        }
        for(var namedParam: t.optionalNamed().entrySet()) { // If any optional named Param is given, it is of right type
            if(c.named().containsKey(namedParam.getKey())) {
                checkType(namedParam.getValue(), c.named().get(namedParam.getKey()));
            }
        }

        hasType(t.returnType());
    }

    @Override
    public Void visitFunctionCall(FunctionCall c) {
        var ftype = inferType(c.callee());
        if(ftype instanceof FunctionTypeRep t) { // check function parameters
            checkFunctionType(t, c);
        } else {
            noFunctionType(ftype, c);
        }

        return null;
    }

    @Override
    public Void visitFunctionExpression(FunctionExpression f) {
        openScope();
        var parameterTypes = f.parameters().types().stream()
            .map(tcomp::compileType)
            .toList();
        var varargsType = tcomp.compileType(f.parameters().varargsType());
        for(int i = 0; i < parameterTypes.size(); i++) {
            declareType(
                    f.parameters().names().get(i).lexeme(), 
                    parameterTypes.get(i),
                    false);
        }
        var optionalParamsTypes = f.parameters().optionals().stream()
            .map(OptionalParam::type)
            .map(tcomp::compileType)
            .toList();
        for(int i = 0; i < optionalParamsTypes.size(); i++) {
            checkType(optionalParamsTypes.get(i), f.parameters().optionals().get(i).defaultValue());
            declareType(
                f.parameters().optionals().get(i).name(),
                optionalParamsTypes.get(i),
                false
            );
        }
        if(f.parameters().varargsName() != null) {
            declareType(
                    f.parameters().varargsName().lexeme(),
                    new ListOfRep(tcomp.compileType(f.parameters().varargsType())),
                    false);
        }
        var namedTypes = tcomp.namedTypesIn(f.parameters().named());
        namedTypes.forEach((var name, var type) -> {
            declareType(name, type, false);
        });
        
        var optionalNamedTypes = tcomp.optionalNamedTypesIn(f.parameters().optionalNamed());
        optionalNamedTypes.forEach((var name , var type) -> {
            declareType(name, type, false);
        });
        var returnType = tcomp.compileType(f.retType());
        var previousReturnType = currentReturnType;
        currentReturnType = returnType;
        checkType(returnType, f.body());
        currentReturnType = previousReturnType;
        closeScope();
        FunctionTypeRep type = new FunctionTypeRep(
                parameterTypes,
                optionalParamsTypes,
                namedTypes,
                optionalNamedTypes,
                varargsType,
                returnType,
                env); 

        hasType(env.normalize(type, this));

        return null;
    }

    @Override
    public Void visitIfExpression(IfExpression i) {
        checkType(booleanType, i.condition());
        checkType(checkTarget, i.thenBranch());
        checkType(checkTarget, i.elseBranch());

        return null;
    }

    @Override
    public Void visitListExpression(ListExpression l) {
        if(checkTarget instanceof ListOfRep lt) {
            for(var p : l.elements()) {
                checkParameter(lt.elements(), p, true);
            }
        } else {
            typeMismatch(checkTarget, inferType(l));
        }
        return null;
    }

    private void hasType(TypeRep t) {
        if(!isAssigneableTo(checkTarget, t)) {
            typeMismatch(checkTarget, t);
        }
    }

    @Override
    public Void visitIndexExpression(IndexExpression i) {
        checkType(numberType, i.index());
        checkType(new ListOfRep(checkTarget), i.list());

        return null;
    }

    TypeRep pushType(ListOfRep l) {
        return new FunctionTypeRep(
                List.of(l.elements()),
                List.of(),
                Map.of(),
                Map.of(),
                null,
                voidType,
                env);
    }

    TypeRep popType(ListOfRep l) {
        return new FunctionTypeRep(
            List.of(),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            l.elements(),
            env);
    }
    TypeRep dequeueType(ListOfRep l) {
        return new FunctionTypeRep(
                List.of(),
                List.of(),
                Map.of(),
                Map.of(),
                null,
                l.elements(),
                env);
    }
    TypeRep peekType(ListOfRep l) {
        return new FunctionTypeRep(
                List.of(),
                List.of(),
                Map.of(),
                Map.of(),
                null,
                l.elements(),
                env);
    }
    TypeRep peekLastType(ListOfRep l) {
        return new FunctionTypeRep(
                List.of(),
                List.of(),
                Map.of(),
                Map.of(),
                null,
                l.elements(),
                env);
    }
    TypeRep prependType(ListOfRep l) {
        return new FunctionTypeRep(
                List.of(l.elements()),
                List.of(),
                Map.of(),
                Map.of(),
                null,
                voidType,
                env);
    }
    TypeRep appendType(ListOfRep l) {
        return new FunctionTypeRep(
                List.of(l),
                List.of(),
                Map.of(),
                Map.of(),
                null,
                voidType,
                env);
    }

    TypeRep listMethodType(String name, ListOfRep l) {
        //System.out.println("got method type of method "+name);
        return switch(name) {
                case "push" -> pushType(l);
                case "pop" -> popType(l);
                case "dequeue" -> dequeueType(l);
                case "peek" -> peekType(l);
                case "peekLast" -> peekLastType(l);
                case "prepend" -> prependType(l);
                case "append" -> appendType(l);
                case "length" -> numberType;
                case "first" -> l.elements();
                case "last" -> l.elements();
                case "rest" -> l;
                case "firsts" -> l;
                default -> {
                    yield unknown();
                }
            };
    }

    @Override
    public Void visitPropertyExpression(PropertyExpression p) {
        var leftType = inferType(p.object());
        if(leftType instanceof ClassType c) {
            if(!c.accessors().containsKey(p.name().lexeme())) {
                error("["+p.name().line()+"] Object '"+this.p.prettyPrint(p.object())+"' has no property '"+p.name().lexeme()+"'");
            } else {
                var accessorType = c.accessors().get(p.name().lexeme());
                if(accessorType instanceof UnknownType) {
                    error("["+p.name().line()+"] Unknown Property Type of property '"+p.name().lexeme()+"'");
                }
                hasType(accessorType);
            }
        } else if(leftType instanceof ListOfRep l) {
            var methodType = listMethodType(p.name().lexeme(), l);
            if(methodType instanceof UnknownType) {
                error("["+p.name().line()+"] List has no property '"+p.name().lexeme()+"'");
            } else {
                hasType(methodType);
            }
        } else if(leftType instanceof MyLangAST.Module m) {
            var type = m.enviroment().getTypeOfValue(p.name().lexeme());
            if(type instanceof UnknownType || (!m.enviroment().valueExported(p.name().lexeme()))) {
                error("["+p.name().line()+"] Module "+m.name()+" does not export '"+p.name().lexeme()+"'");
            } else {
                hasType(type);
            }
        } else {
            error("["+p.name().line()+"] Type "+showType(leftType)+" has no properties");
        }

        return null;
    }

    @Override
    public Void visitBlockExpression(BlockExpression b) {
        openScope();
        for(DeclarationOrStatement d : b.statements()) {
            declare(d);

            checkDeclOrStatement(d);
        }

        checkType(checkTarget, b.returnValue());

        closeScope();

        return null;
    }

    @Override
    public Void visitWhileDoExpression(WhileDoExpression wd) {
        checkType(booleanType, wd.condition());
        checkStatement(wd.body());

        hasType(voidType);
        return null;
    }

    @Override
    public Void visitWhileYieldExpression(WhileYieldExpression wy) {
        checkType(booleanType, wy.condition());
        if(checkTarget instanceof ListOfRep l) {
            checkParameter(l.elements(), wy.body(), true);
        } else {
            var bodyType = new ListOfRep(inferElemTypeOfParameter(wy.body(), false));
            typeMismatch(checkTarget, bodyType);
        }

        return null;
    }

    @Override
    public Void visitForDoExpression(ForDoExpression fd) {
        var collectionType = inferType(fd.collection());
        if(collectionType instanceof ListOfRep l) {
            openScope();
            declareType(fd.variable().lexeme(), l.elements(), false);
            checkType(booleanType, fd.guard());
            checkStatement(fd.body());
            hasType(voidType);
            closeScope();
        } else {
            error("["+fd.variable().line()+"] Cannot iterate values of type '"+showType(collectionType)+"'");
        }
        return null;
    }

    @Override
    public Void visitForYieldExpression(ForYieldExpression fy) {
        var collectionType = inferType(fy.collection());
        if(collectionType instanceof ListOfRep l) {
            openScope();
            declareType(fy.variable().lexeme(), l.elements(), false);
            checkType(booleanType, fy.guard());
            if(checkTarget instanceof ListOfRep l2) {
                checkParameter(l2.elements(), fy.body(), true);
            } else {
                typeMismatch(checkTarget, new ListOfRep(inferElemTypeOfParameter(fy.body(), false)));
            }
        } else {
            error("["+fy.variable().line()+"] Cannot iterate values of type '"+showType(collectionType)+"'");
        }
        return null;
    }

    static TypeRep rangeType = new ListOfRep(numberType);

    @Override
    public Void visitRangeExpression(RangeExpression r) {
        checkType(numberType, r.start());
        checkType(numberType, r.end());
        checkType(numberType, r.step());
        hasType(rangeType);
        return null;
    }

    @Override
    public Void visitThisExpression(ThisExpression t) {
        var currentThisType = getTypeOf(t.keyword());
        if(currentThisType instanceof UnknownType) {
            error("["+t.keyword().line()+"] Cannot use this outside of methods");
        }
        hasType(currentThisType);
        return null;
    }

    @Override
    public Void visitEmptyStatement(EmptyStatement e) {
        return null;
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatement s) {
        checkType(voidType, s.expression());
        return null;
    }
    
    @Override
    public Void visitSetStatement(SetStatement s) {
        var assignedType = getTypeOf(s.name());
        var isReassignable = isReassignable(s.name().lexeme());
        if(!isReassignable) {
            error("["+s.name().line()+"] Cannot reassign variable '"+s.name().lexeme()+"' because it is declared as immutable");
        }
        if(assignedType instanceof UnknownType) {
            error("["+s.name().line()+"] Tried to reassign undeclared variable '"+s.name().lexeme()+"'");
        }

        checkType(assignedType, s.expression());
        
        return null;
    }

    @Override
    public Void visitSetIndexStatement(SetIndexStatement si) {
        var collectionType = inferType(si.list());
        checkType(numberType, si.index());
        if(collectionType instanceof ListOfRep l) {
            checkType(l.elements(), si.expression());
        } else {
            typeMismatch(new ListOfRep(inferType(si.expression())), collectionType);
        }

        return null;
    }

    @Override
    public Void visitSetPropertyStatement(SetPropertyStatement p) {
        var classType = inferType(p.target());
        if(classType instanceof ClassType t) {
            if(!t.accessors().containsKey(p.name().lexeme())) {
                error("["+p.name().line()+"] Cannot set property '"+
                        p.name().lexeme()+"' because Object has no property of this name");
                return null;
            }
            var propertyType = t.accessors().get(p.name().lexeme());
            checkType(propertyType, p.expression());
            var isReassignable = t.readability().get(p.name().lexeme());
            if(!isReassignable && !(p.target() instanceof ThisExpression && inConstructor)) {
                error("["+p.name().line()+"] Cannot reassign property '"+p.name().lexeme()+"' because it's immutable");
            }
        } else {
            error("["+p.name().line()+"] Cannot assign to property because Value is not an object but an '"+showType(classType)+"'");
        }
        return null;
    }

    @Override
    public Void visitTypeDefDeclaration(TypeDefDeclaration t) {
        env.normalize(env.getTypeByName(t.Name().lexeme()), this);
        return null;
    }

    @Override
    public Void visitIfStatement(IfStatement i) {
        checkType(booleanType, i.condition());
        checkStatement(i.body());
        return null;
    }
    @Override
    public Void visitReturnStatement(ReturnStatement r) {
        if(currentReturnType == null) {
            error("Cannot return outside of Function");
            return null;
        }
        checkType(currentReturnType, r.returnValue());

        return null;
    }
    
    @Override
    public Void visitEnumDeclaration(EnumDeclaration e) {
        for(var constructor : e.variants()) { // Check that all Constructors are Well-Formed
            env.normalize(enumConstructorTypeOf(constructor, e.Name()), this); 
        }
        return null;
    }
}
