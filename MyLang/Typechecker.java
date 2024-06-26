package MyLang;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import static MyLang.MyLangAST.*;


public class Typechecker implements 
        DeclarationVisitor<Void>, ExpressionVisitor<Void>, StatementVisitor<Void>, PatternVisitor<Void>, SetterVisitor<Void> {
    private boolean hadError = false;
    private List<String> errors = new ArrayList<>();

    TypeEnv env = new TypeEnv();
    PrettyPrinter p = new PrettyPrinter();
    SubtypeChecker s = new SubtypeChecker();
    TypeInferrer ti = new TypeInferrer(this);
    TypeCompiler tcomp = new TypeCompiler(this);
    TypeApplier ta = new TypeApplier(this);

    private TypeRep checkTarget = null;
    TypeRep currentReturnType = null;
    private boolean exportCurrentPatterns = false;
    private boolean isMutable = false;

    MyLangRunner runner;

    private String currentFileName = null;

    private boolean inConstructor = false;
    private boolean inClass = false;

    public Typechecker(MyLangRunner runner) {
        this.runner = runner;

        declareBuiltins();
    }

    public static boolean typechecks(MyLangRunner runner, MyLangFile program, String name) {
        Typechecker tc = new Typechecker(runner);
        tc.currentFileName = runner.unresolve(name);
        tc.gatherImports(program.imports());
        tc.gatherTypes(program.declarations());


        for(Declaration decl: program.declarations()) {
            tc.checkDeclaration(decl);
        }

        runner.typecheckedFiles.put(name, new MyLangAST.Module(tc.currentFileName, tc.env));
        if(tc.hadError) {
            System.out.println("Following errors occured:");
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
        if(checkTarget instanceof UnknownType) {
            for(var err : errors) {
                System.out.println(err);
            }
            throw new RuntimeException("Tried checking of unknown type: "+p.prettyPrint(tested)+" or: "+tested);
        }

        tested.accept(this);

        checkTarget = previousTarget;
    }

    private TypeRep inferType(Expression e) {
        var resultType = ti.infer(e);
        if(resultType instanceof UnknownType) {
            error("Could not infer type of expression "+p.prettyPrint(e));
        }
        checkType(resultType, e);
        
        //System.out.println("inferred type "+showType(resultType)+" of expression "+e);
        return env.normalize(resultType, this);
    }
    private TypeRep inferSetterType(Setter s) {
        var setterType = ti.inferSetter(s);
        checkSetter(s, setterType);
        return setterType;
    }

    private void checkSetter(Setter s, TypeRep type) {
        var previousTarget = checkTarget;
        checkTarget = type;
        if(type instanceof UnknownType) {
            throw new RuntimeException("Tried checking of unknown type in "+p.prettyPrint(s)+" or: "+s);
        }
        s.accept(this);
        checkTarget = previousTarget;
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
            checkIrrefutablePattern(v.pat(), variableDeclarationTypeOf(v), v.export(), v.isReassignable());
        } else if(decl instanceof ValElseDeclaration v) {
            var matched = inferType(v.initializer());
            checkPattern(matched, v.pat(), false);
        } else if(decl instanceof FunctionDeclaration f) {
            declareType(f.Name().lexeme(), functionTypeOf(f), false);
            if(f.export()) {
                env.exportValue(f.Name().lexeme());
            }
        } else if(decl instanceof ClassDeclaration c) {
            declareNewType(c.Name().lexeme(), classTypeOf(c));
            declareType(c.Name().lexeme(), constructorTypeOf(c, false), false);
            if(c.export()) {
                env.exportValue(c.Name().lexeme());
                env.exportType(c.Name().lexeme());
            }
        } else if(decl instanceof TypeDefDeclaration t) {
            var definition = typeDefType(t.args(), t.definition());
            declareNewType(t.Name().lexeme(), definition);
            if(t.export()) {
                env.exportType(t.Name().lexeme());
            }
        } else if(decl instanceof EnumDeclaration e) {
            TypeIdentifierRep ti = new TypeIdentifierRep(e.Name(), env);
            declareNewType(e.Name().lexeme(), ti); // Declare placeholder type
            for(var constructor: e.variants()) {
                declareType(constructor.name().lexeme(),
                    enumConstructorTypeOf(e.args(), constructor, e.Name(), true), false);
                if(e.export()) {
                    env.exportValue(constructor.name().lexeme());
                }
            }
            env.addType(e.Name().lexeme(), enumTypeOf(e)); // Overwrite with actual type
            if(e.export()) {
                env.exportType(e.Name().lexeme());
            }
        }
    }

    private TypeRep typeDefType(List<Token> args, Type definition) {
        if(args == null) {
            return tcomp.compileType(definition);
        } else {
            openScope();
            for(var param : args) {
                declareNewType(
                    param.lexeme(), 
                    new TypeVar(param, env));
            }
            var body = env.normalize(tcomp.compileType(definition), this);
            closeScope();
            return new TypeFunction(args, body, env);
        }
    }

    private TypeRep variableDeclarationTypeOf(VariableDeclaration v) {
        if(v.type() == null) {
            return inferType(v.initializer());
        } else {
            return tcomp.compileType(v.type());
        }
    }

    private void checkIrrefutablePattern(Pattern p, TypeRep t, boolean export, boolean isReassigneable) {
        if(isIrrefutable(p)) {
            var previousExport = exportCurrentPatterns;
            exportCurrentPatterns = export;
            checkPattern(t, p, isReassigneable);
            exportCurrentPatterns = previousExport;
        }
    }
    private boolean isIrrefutable(Pattern p) {
        if(p instanceof Wildcard || p instanceof VariableBinding) {
            return true;
        } else if(p instanceof TuplePattern t) {
            return t.subPatterns().stream().allMatch(this::isIrrefutable);
        } else {
            return false;
        }
    }

    TypeRep enumTypeOf(EnumDeclaration e) {
        if(e.args() == null) {
            return new EnumType(e.Name(), 
                e.variants().stream().collect(Collectors.toMap(
                    c -> c.name().lexeme(), 
                    c -> enumConstructorTypeOf(e.args(), c, e.Name(), false)
                    )
                ),
                e.methods().stream().collect(Collectors.toMap(
                    d -> d.Name().lexeme(), 
                    d -> functionTypeOf(d))),
                env);
        } else {
            openScope();
            for(var param : e.args()) {
                declareNewType(param.lexeme(), new TypeVar(param, env));
            }
            var body = new EnumType(e.Name(), 
                e.variants().stream().collect(Collectors.toMap(
                    c -> c.name().lexeme(), 
                    c -> enumConstructorTypeOf(e.args(), c, e.Name(), false)
                    )
                ),
                e.methods().stream().collect(Collectors.toMap(
                    d -> d.Name().lexeme(), 
                    d -> functionTypeOf(d))),
                env);
            closeScope();
            return new TypeFunction(e.args(), body, env);
        }
    }
    TypeRep enumConstructorTypeOf(List<Token> args, EnumConstructor e, Token enumName, boolean function) {
        openScope();
        if(args != null) {
            for(var param : args) {
                declareNewType(param.lexeme(), new TypeVar(param, env));
            }
        } 
        TypeRep returnType = new TypeIdentifierRep(enumName, env);
        if(args != null) {
            returnType = new TypeApplication(
                returnType,
                args.stream()
                    .map(p -> (TypeRep) new TypeVar(p, env))
                    .toList()
            );
        }
        var body = new FunctionTypeRep(
            e.parameters().stream().map(tcomp::compileType).toList(),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            returnType,
            env
        );
        closeScope();
        if(args != null && function) {
            return new GenericType(new TypeFunction(args, body, env));
        } else {
            return body;
        }
    }

    TypeRep functionTypeOf(FunctionDeclaration f) {
        openScope();
        for(var typeParam : f.typeParams()) {
            declareNewType(typeParam.lexeme(), new TypeVar(typeParam, env));
        }
        var type = new FunctionTypeRep(
                f.parameters().types().stream().map(tcomp::compileType).toList(),
                f.parameters().optionals().stream().map(OptionalParam::type).map(tcomp::compileType).toList(),
                tcomp.namedTypesIn(f.parameters().named()),
                tcomp.optionalNamedTypesIn(f.parameters().optionalNamed()),
                tcomp.compileType(f.parameters().varargsType()),
                tcomp.compileType(f.retType()),
                env);
        closeScope();
        if(!f.typeParams().isEmpty()) {
            var result = new GenericType(new TypeFunction(f.typeParams(), type, env));
            return result;
        } else {
            return type;
        }
    }

    TypeRep classTypeOf(ClassDeclaration c) {
        if(c.args() != null) {
            openScope();
            for(var param: c.args()) {
                declareNewType(
                    param.lexeme(), 
                    new TypeVar(param, env));
            }
        }
        TypeRep type = new ClassType(
                    c.Name(),
                    accessorsIn(c),
                    getReadability(c.fieldsAndMethods()),
                    constructorTypeOf(c, true),
                    env
                );
        if(c.args() != null) {
            type = new TypeFunction(c.args(), type, env);
            closeScope();
        }
        return type;
    }

    Map<String, Boolean> getReadability(List<Declaration> decls) {
        Map<String, Boolean> map = new HashMap<>();
        for(Declaration decl: decls) {
            if(decl instanceof VariableDeclaration v) {
                if(v.pat() instanceof VariableBinding b) {
                    map.put(b.name().lexeme(), v.isReassignable());
                } else {
                    error("Cannot use Patterns in Field declarations");
                }
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
    TypeRep constructorTypeOf(ClassDeclaration c, boolean inClass) {
        TypeRep constructor;
        TypeRep retType = 
                    new TypeIdentifierRep(c.Name(), env);
        if(c.args() != null) {
            if(!inClass) {
                openScope();
            }
            retType = new TypeApplication(retType,
                c.args().stream()
                    .map(a -> (TypeRep) new TypeVar(a, env))
                    .toList());
            if(!inClass) {
                for(var param : c.args()) {
                    declareNewType(param.lexeme(), new TypeVar(param, env));
                }
            }
        }
        if(c.constructor() == null) { // Standard Constructor
            constructor =  new FunctionTypeRep(
                    List.of(),
                    List.of(),
                    Map.of(),
                    Map.of(),
                    null,
                    retType,
                    env);
        } else
            constructor =  new FunctionTypeRep(
                c.constructor().parameters().types().stream().map(tcomp::compileType).toList(),
                c.constructor().parameters().optionals().stream().map(OptionalParam::type).map(tcomp::compileType).toList(),
                tcomp.namedTypesIn(c.constructor().parameters().named()),
                tcomp.optionalNamedTypesIn(c.constructor().parameters().optionalNamed()),
                tcomp.compileType(c.constructor().parameters().varargsType()),
                retType,
                env
                );

        if(c.args() != null && !inClass) {
            closeScope();
            return new GenericType(new TypeFunction(c.args(), constructor, env));
        } else {
            return constructor;
        }
    }

    void declareType(String name, TypeRep type, boolean isReassignable) {
        env.addValue(name, type, isReassignable);
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclaration value) {
        if(inClass && value.initializer() instanceof NullLiteral) {
            return null;
        }
        checkType(variableDeclarationTypeOf(value), value.initializer());
        return null;
    }
    @Override
    public Void visitValElseDeclaration(ValElseDeclaration v) {
        if(inClass) {
            error("ValElse not allowed as Field declaration");
        }
        var matched = inferType(v.initializer());
        checkType(neverType, v.elseBranch());
        checkPattern(matched, v.pat(), false);
        return null;
    }

    @Override
    public Void visitFunctionDeclaration(FunctionDeclaration value) {
        openScope();
            for(var typeParam : value.typeParams()) {
                declareNewType(typeParam.lexeme(), new TypeVar(typeParam, env));
            }
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
        var classType = env.getTypeByName(value.Name().lexeme(), this);

        openScope();
            boolean prevInClass = inClass;
            inClass = true; 
            if(value.args() != null) {
                classType = env.normalize(new TypeApplication(classType,
                    value.args().stream()
                        .map(a -> (TypeRep) new TypeVar(a, env))
                        .toList()), this);
            }
            declareType("this", classType, false);
            if(value.args() != null) {
                for(var arg : value.args()) {
                    declareNewType(arg.lexeme(), new TypeVar(arg, env));
                }    
            }
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

    void typeMismatch(TypeRep expected, TypeRep given, String expr) {
        error("Expected '"+showType(expected)+"', got '"+showType(given)+"' in '"+expr+"'");
    }

    boolean isAssigneableTo(TypeRep to, TypeRep from, String expr) {
        if(to == null || from == null) {
            hadError = true;
            return true;
        }
        var toBefore = to;
        var fromBefore = from;
        to = env.normalize(to, this);
        from = env.normalize(from, this);
        if(to.equals(unknown()) || from.equals(unknown())) {
            hadError = true;
            System.out.println("to: "+to);
            System.out.println("to before: "+toBefore);
            System.out.println("from: "+from);
            System.out.println("from before: "+fromBefore);
            for(var error : errors) {
                System.out.println(error);
            }
            throw new RuntimeException("Had unknown" + (to.equals(unknown()) ? " to" : " from") + "in expression" + expr);
        }
        if(to.equals(voidType)) {
            return true;
        }
        if(from instanceof TypeFunction t) {
            error("Cannot use type function as real type: "+p.prettyPrint(t));
            return true;
        }
        if(to instanceof TypeFunction t) {
            error("Cannot use type function as real type: "+p.prettyPrint(t));
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
        hasType(numberType, p.prettyPrint(value));
        return null;
    }

    static TypeRep stringType = new Builtin(BuiltinType.STRING);

    @Override
    public Void visitStringLiteral(StringLiteral value) {
        hasType(stringType, p.prettyPrint(value));
        return null;
    }

    static TypeRep booleanType = new Builtin(BuiltinType.BOOLEAN);

    @Override
    public Void visitBooleanLiteral(BooleanLiteral value) {
        hasType(booleanType, p.prettyPrint(value));
        return null;
    }

    static TypeRep voidType = new Builtin(BuiltinType.VOID);

    @Override
    public Void visitNullLiteral(NullLiteral value) {
        hasType(voidType, "null");
        return null;
    }

    void unknownVariable(Token name) {
        error("["+name.line()+"] Unknown variable '"+name.lexeme()+"'");
    }

    static TypeRep unknown() {
        return new UnknownType();}

    TypeRep getTypeOf(Token name) {
        return getTypeOf(name, false);
    }
    TypeRep getTypeOf(Token name, boolean inferMode) {
        var result = env.getTypeOfValue(name.lexeme(), this);
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
            hasType(getTypeOf(value.value()), value.value().lexeme());
        }
        return null;
    }

    @Override 
    public Void visitBinaryOperation(BinaryOperation b) {
        switch(b.operator().type()) {
            case AND, OR, XOR -> {
                checkType(booleanType, b.left());
                checkType(booleanType, b.right());

                hasType(booleanType, p.prettyPrint(b));
            }
            case PLUS -> {
                var leftType = inferType(b.left());
                if(leftType.equals(stringType)) {
                    checkType(voidType, b.right());
                    hasType(stringType, p.prettyPrint(b));
                } else {
                    var rightType = inferType(b.right());
                    if(rightType.equals(stringType)) {
                        hasType(stringType, p.prettyPrint(b));
                    } else if(leftType.equals(numberType) && rightType.equals(numberType)) {
                        hasType(numberType, p.prettyPrint(b));
                    } else {
                        error("["+b.operator().line()+"] Expected Number or String, got "+showType(leftType));
                        error("["+b.operator().line()+"] Expected Number or String, got "+showType(rightType));
                    }
                }
            }
            case MINUS, STAR, SLASH, PERCENT, EXPO -> {
                checkType(numberType, b.left());
                checkType(numberType, b.right());

                hasType(numberType, p.prettyPrint(b));
            }
            case GREATER, GREATER_EQUAL, LESS, LESS_EQUAL -> {
                checkType(numberType, b.left());
                checkType(numberType, b.right());

                hasType(booleanType, p.prettyPrint(b));
            }
            case EQUAL, NOT_EQUAL -> {
                var leftType = inferType(b.left());
                checkType(leftType, b.right());

                hasType(booleanType, p.prettyPrint(b));
            }
            case IN -> {
                TypeRep elementType = inferType(b.left());
                checkType(new ListOfRep(elementType), b.right());

                hasType(booleanType, p.prettyPrint(b));
            }
            default -> throw new RuntimeException("Unknown binary operator: "+b.operator().lexeme());
        }

        return null;
    }

    @Override
    public Void visitUnaryOperation(UnaryOperation o) {
        if(o.operator().type() == TokenType.BANG) {
            checkType(booleanType, o.operand());
            hasType(booleanType, p.prettyPrint(o));
        } else if(o.operator().type() == TokenType.PLUS || o.operator().type() == TokenType.MINUS) {
            checkType(numberType, o.operand());
            hasType(numberType, p.prettyPrint(o));
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
                        " (type: "+showType(t)+")" + " in expression "+p.prettyPrint(c));
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
        if(t.returnType() instanceof UnknownType) {
            System.out.println(t);
        }

        hasType(t.returnType(), p.prettyPrint(c));
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
        FunctionTypeRep functiontype = compileAndReplace(checkTarget, f);
        openScope();
            var parameterTypes = functiontype.parameters();
            var varargsType = functiontype.varargsType();
            for(int i = 0; i < parameterTypes.size(); i++) {
                declareType(
                    f.parameters().names().get(i).lexeme(), 
                    parameterTypes.get(i),
                    false);
            }
            var optionalParamsTypes = functiontype.optionalParameters();
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
            var namedTypes = functiontype.named();
            namedTypes.forEach((var name, var type) -> {
                declareType(name, type, false);
            });
        
            var optionalNamedTypes = functiontype.optionalNamed();
            optionalNamedTypes.forEach((var name , var type) -> {
                declareType(name, type, false);
            });
            var returnType = functiontype.returnType();
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

        hasType(env.normalize(type, this), p.prettyPrint(f));

        return null;
    }
    private FunctionTypeRep compileAndReplace(TypeRep t, FunctionExpression f) {
        List<TypeRep> parameterTypes = null;
        TypeRep retType = tcomp.compileType(f.retType());
        if(t instanceof FunctionTypeRep ftype) {
            var params = f.parameters();
            var origParams = params.types();
            parameterTypes = new ArrayList<>();
            for(int i = 0; i < params.types().size(); i++) {
                if(origParams.get(i) != null) {
                    parameterTypes.add(tcomp.compileType(origParams.get(i)));
                } else if(ftype.parameters().size() <= i) {
                    parameterTypes.add(unknown());
                } else {
                    parameterTypes.add(ftype.parameters().get(i));
                }
            }
            if(retType == null) {
                retType = ftype.returnType();
            }
        } else {
            parameterTypes = f.parameters().types().stream()
                .map(tcomp::compileType)
                .toList();
        } 
        return new FunctionTypeRep(
            parameterTypes,
            f.parameters().optionals().stream()
                .map(OptionalParam::type)
                .map(tcomp::compileType)
                .toList(),
            f.parameters().named().entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), tcomp.compileType(entry.getValue())))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                )
            ),
            f.parameters().optionalNamed().entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), tcomp.compileType(entry.getValue().type())))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                )
            ),
            tcomp.compileType(f.parameters().varargsType()),
            retType,
            env
        );
    }

    @Override
    public Void visitIfExpression(IfExpression i) {
        checkType(booleanType, i.condition());
        checkType(checkTarget, i.thenBranch());
        checkType(checkTarget, i.elseBranch());

        return null;
    }

    @Override
    public Void visitIfValExpression(IfValExpression i) {
        var matchedType = inferType(i.matched());
        openScope();
            checkPattern(matchedType, i.pat(), false);
            checkType(checkTarget, i.thenBranch());
        closeScope();
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
            typeMismatch(checkTarget, inferType(l), p.prettyPrint(l));
        }
        return null;
    }

    private void hasType(TypeRep t, String expr) {
        if(checkTarget instanceof UnknownType || checkTarget == null || checkTarget.equals(unknown())) {
            throw new RuntimeException("Tried checking of unknown type");
        }
        if(!isAssigneableTo(checkTarget, t, expr)) {
            typeMismatch(checkTarget, t, expr);
        }
    }

    @Override
    public Void visitIndexExpression(IndexExpression i) {
        checkType(numberType, i.index());
        if(inferType(i.list()) instanceof Builtin b) {
            if(b.type().equals(BuiltinType.STRING)) {
                hasType(stringType, p.prettyPrint(i));
                return null;
            }
        }
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
                case "push" -> pushType(l); //
                case "pop" -> popType(l);   //
                case "dequeue" -> dequeueType(l); //
                case "peek" -> peekType(l); /* */
                case "peekLast" -> peekLastType(l); //
                case "prepend" -> prependType(l); //
                case "append" -> appendType(l);
                case "length" -> numberType; // 
                case "first" -> l.elements(); //
                case "last" -> l.elements(); //
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
                hasType(accessorType, this.p.prettyPrint(p));
            }
        } else if(leftType instanceof ListOfRep l) {
            var methodType = listMethodType(p.name().lexeme(), l);
            if(methodType instanceof UnknownType) {
                error("["+p.name().line()+"] List has no property '"+p.name().lexeme()+"'");
            } else {
                hasType(methodType, this.p.prettyPrint(p));
            }
        } else if(leftType instanceof MyLangAST.Module m) {
            var type = m.enviroment().getTypeOfValue(p.name().lexeme(), this);
            if(type instanceof UnknownType || /*(!m.enviroment().valueExported(p.name().lexeme()))*/ false) {
                error("["+p.name().line()+"] Module "+m.name()+" does not export '"+p.name().lexeme()+"'");
            } else {
                hasType(type, this.p.prettyPrint(p));
            }
        } else if(leftType instanceof EnumType e) {
            if(!e.methods().containsKey(p.name().lexeme())) {
                error("["+p.name().line()+"]: Object "+this.p.prettyPrint(p.object())+" has no property "+p.name().lexeme());
            } else {
                var type = e.methods().get(p.name().lexeme());
                hasType(type, this.p.prettyPrint(p));
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
                checkAndDeclare(d);            
            }

            checkType(checkTarget, b.returnValue());

        closeScope();

        return null;
    }
    private void checkAndDeclare(DeclarationOrStatement dors) {
        if(dors instanceof Statement s) {
            checkStatement(s);
        } else if(dors instanceof Declaration d) {
            if(d instanceof FunctionDeclaration || d instanceof ClassDeclaration || d instanceof EnumDeclaration) {
                declare(d);
                checkDeclaration(d);
            } else {
                checkDeclaration(d);
                declare(d);
            }
        }
    }

    @Override
    public Void visitWhileDoExpression(WhileDoExpression wd) {
        checkType(booleanType, wd.condition());
        checkStatement(wd.body());

        hasType(voidType, p.prettyPrint(wd));
        return null;
    }
    @Override
    public Void visitWhileValDoExpression(WhileValDoExpression w) {
        var matched = inferType(w.matched());
        openScope();
            checkPattern(matched, w.pattern(), false);
            checkType(voidType, w.body());
        closeScope();

        hasType(voidType, p.prettyPrint(w));
        return null;
    }

    @Override
    public Void visitWhileYieldExpression(WhileYieldExpression wy) {
        checkType(booleanType, wy.condition());
        if(checkTarget instanceof ListOfRep l) {
            checkParameter(l.elements(), wy.body(), true);
        } else {
            var bodyType = new ListOfRep(inferElemTypeOfParameter(wy.body(), false));
            typeMismatch(checkTarget, bodyType, p.prettyPrint(wy));
        }

        return null;
    }
    @Override
    public Void visitWhileValYieldExpression(WhileValYieldExpression wy) {
        var matched = inferType(wy.matched());
        openScope();
            checkPattern(matched, wy.pattern(), false);
            if(checkTarget instanceof ListOfRep l) {
                checkType(l.elements(), wy.body());
            } else {
                var bodyType = inferType(wy.body());
                typeMismatch(checkTarget, new ListOfRep(bodyType), p.prettyPrint(wy));
            } 
        closeScope();

        return null;
    }

    @Override
    public Void visitForDoStatement(ForDoStatement fd) {
        var collectionType = inferType(fd.collection());
        if(collectionType instanceof ListOfRep l) {
            openScope();
                checkPattern(l.elements(), fd.pat(), false);
                checkType(booleanType, fd.guard());
                checkType(voidType, fd.body());
            closeScope();
        } else {
            error("Cannot iterate values of type '"+showType(collectionType)+"'");
        }
        return null;
    }

    @Override
    public Void visitForYieldExpression(ForYieldExpression fy) {
        var collectionType = inferType(fy.collection());
        if(collectionType instanceof ListOfRep l) {
            openScope();
                checkPattern(l.elements(), fy.pat(), false);
                checkType(booleanType, fy.guard());
                if(checkTarget instanceof ListOfRep l2) {
                    checkParameter(l2.elements(), fy.body(), true);
                } else {
                    typeMismatch(checkTarget, new ListOfRep(inferElemTypeOfParameter(fy.body(), false)), p.prettyPrint(fy));
                }
            closeScope();
        } else {
            error(" Cannot iterate values of type '"+showType(collectionType)+"'");
        }
        return null;
    }

    static TypeRep rangeType = new ListOfRep(numberType);

    @Override
    public Void visitRangeExpression(RangeExpression r) {
        checkType(numberType, r.start());
        checkType(numberType, r.end());
        checkType(numberType, r.step());
        hasType(rangeType, p.prettyPrint(r));
        return null;
    }

    @Override
    public Void visitThisExpression(ThisExpression t) {
        var currentThisType = getTypeOf(t.keyword());
        if(currentThisType instanceof UnknownType) {
            error("["+t.keyword().line()+"] Cannot use this outside of methods");
        }
        hasType(currentThisType, "this");
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
        TypeRep assignedType = inferSetterType(s.setter());
        if(assignedType instanceof UnknownType) {
            error("Setter has unknown type in "+p.prettyPrint(s));
            unknown();
        }
        checkType(assignedType, s.expression());
        return null;
    }

    @Override
    public Void visitTypeDefDeclaration(TypeDefDeclaration t) {
        env.normalize(env.getTypeByName(t.Name().lexeme(), this), this);
        return null;
    }

    @Override
    public Void visitIfStatement(IfStatement i) {
        checkType(booleanType, i.condition());
        checkStatement(i.body());
        return null;
    }
    public static TypeRep neverType = new Never();
    @Override
    public Void visitReturnExpression(ReturnExpression r) {
        if(currentReturnType == null) {
            error("Cannot return outside of Function");
            return null;
        }
        checkType(currentReturnType, r.returnValue());
        hasType(neverType, p.prettyPrint(r));

        return null;
    }
    
    @Override
    public Void visitEnumDeclaration(EnumDeclaration e) {
        for(var constructor : e.variants()) { // Check that all Constructors are Well-Formed
            env.normalize(enumConstructorTypeOf(e.args(), constructor, e.Name(), false), this); 
        }
        var enumType = env.getTypeByName(e.Name().lexeme(), this);

        openScope();
            boolean prevInClass = inClass;
            inClass = true;

           if(e.args() != null && !e.args().isEmpty()) {
                enumType = new TypeApplication(
                    enumType, 
                    e.args().stream().map(x -> (TypeRep) new TypeIdentifierRep(x, env)).toList()
                );
            }

            declareType("this", enumType, false);

            if(e.args() != null)
                for(var x : e.args()) {
                    declareNewType(x.lexeme(), new TypeVar(x, env));
                }

            
            for(var method : e.methods()) {
                checkDeclaration(method);    
            }

            inClass = prevInClass;
        return null;
    }

    @Override
    public Void visitMatchExpression(MatchExpression m) {
        var matchedType = inferType(m.matched());
        for(int i = 0; i < m.cases().size(); i++) {
            openScope();
                checkPattern(matchedType, m.cases().get(i), false);
                checkType(checkTarget, m.branches().get(i));
            closeScope();
        }

        return null;
    }

    private void checkPattern(TypeRep t, Pattern p, boolean isReassigneable) {
        var previousTarget = checkTarget;
        checkTarget = t;
        if(checkTarget instanceof UnknownType) {
            throw new RuntimeException("Tried checking of unknown type");
        }
        var previousIsReassigneable = isMutable;
        isMutable = isReassigneable;
        p.accept(this);
        isMutable = previousIsReassigneable;
        checkTarget = previousTarget;
    }

    @Override
    public Void visitWildcard(Wildcard w) {
        return null;
    }
    @Override
    public Void visitNumberPattern(NumberPattern n) {
        hasType(numberType, n.value()+"");
        return null;
    }
    @Override
    public Void visitBooleanPattern(BooleanPattern b) {
        hasType(booleanType, b.value()+"");
        return null;
    }
    @Override
    public Void visitStringPattern(StringPattern s) {
        hasType(stringType, p.prettyPrint(new StringLiteral(s.value())));
        return null;
    }
    @Override
    public Void visitVariableBinding(VariableBinding v) {
        declareType(
            v.name().lexeme(), 
            checkTarget, 
            isMutable);
        if(exportCurrentPatterns) {
            env.exportValue(v.name().lexeme());
        }
        return null;
    }
    @Override
    public Void visitConstructorPattern(ConstructorPattern p) {
        if(env.normalize(checkTarget, this) instanceof EnumType e) {
            if(!e.variants().containsKey(p.constr().lexeme())) {
                error("Type '"+checkTarget+"' has no constructor '"+p.constr().lexeme()+"'");
            } else {
                var constructorType = (FunctionTypeRep) e.variants().get(p.constr().lexeme());
                if(constructorType.parameters().size() != p.subPatterns().size()) {
                    error("Wrong number of arguments in pattern of constructor '"+p.constr().lexeme()+
                        "': Expected "+constructorType.parameters().size()+", got "+p.subPatterns().size());
                } else {
                    for(int i = 0; i < p.subPatterns().size(); i++) {
                        checkPattern(constructorType.parameters().get(i), p.subPatterns().get(i), false);
                    }
                }
            }
        } else {
            error("Cannot match on Constructor " + p.constr().lexeme() + " of non-enum type '"+
                this.p.prettyPrint(checkTarget)+"'");
        }
        return null;
    }
    @Override
    public Void visitTupleExpression(TupleExpression t) {
        if(checkTarget instanceof TupleRep t2) {
            if(t2.elements().size() != t.elements().size()) {
                error("Expected Tuple with "+t2.elements().size()+" elements, but got "+t.elements().size()+" elements");
                return null;
            }
            for(int i = 0; i < t.elements().size(); i++) {
                checkType(t2.elements().get(i), t.elements().get(i));
            }
        } else if(checkTarget.equals(voidType)) {
            inferType(t);
        } else {
            typeMismatch(checkTarget, new TupleRep(t.elements().stream().map(this::inferType).toList()), p.prettyPrint(t));
        }
        return null;
    }
    @Override
    public Void visitTuplePattern(TuplePattern p) {
        if(checkTarget instanceof TupleRep t) {
            if(t.elements().size() != p.subPatterns().size()) {
                error("Expected Tuple with "+p.subPatterns().size()+"elements, got Tuple with "+t.elements().size()+": "+this.p.prettyPrint(t));
                return null;
            }
            for(int i = 0; i < t.elements().size(); i++) {
                checkPattern(t.elements().get(i), p.subPatterns().get(i), isMutable);
            }
        } else {
            error("Tried to match on non-tuple type "+this.p.prettyPrint(checkTarget) + "in pattern "+this.p.prettyPrint(p));
        }
        return null;
    }
    @Override
    public Void visitWildcardExpression(WildcardExpression w) {
        error("Got wildcard expression of type "+p.prettyPrint(checkTarget)+"on line "+w.position().line());
        return null;
    }
    @Override
    public Void visitWildcardSetter(WildcardSetter s) {
        return null;
    }
    @Override
    public Void visitTupleSetter(TupleSetter s) {
        if(checkTarget instanceof TupleRep t) {
            if(t.elements().size() != s.setters().size()) {
                typeMismatch(checkTarget, ti.inferSetter(s), p.prettyPrint(s));
                return null;
            }
            for(int i = 0; i < t.elements().size(); i++) {
                checkSetter(s.setters().get(i), t.elements().get(i));
            }
            return null;
        } else {
            typeMismatch(checkTarget, ti.inferSetter(s), p.prettyPrint(s));
            return null;
        }
    }
    @Override
    public Void visitIndexSetter(IndexSetter i) {
        checkType(new ListOfRep(checkTarget), i.list());
        checkType(numberType, i.index());
        return null;
    }
    @Override
    public Void visitPropertySetter(PropertySetter p) {
        var objectType = inferType(p.object());
        if(objectType instanceof ClassType c) {
            var propertyType = c.accessors().get(p.name().lexeme());
            if(propertyType == null) {
                error("["+p.name().line()+"] Type "+this.p.prettyPrint(c)+" has no property  of name "+p.name().lexeme());
            }
            if(propertyType instanceof UnknownType) {
                error("Property had unknown type in "+this.p.prettyPrint(p));
            }
            var isReassignable = c.readability().get(p.name().lexeme());
            if(!isReassignable && !inConstructor) {
                error("["+p.name().line()+"]: Property "+
                    p.name().lexeme()+" of type "+this.p.prettyPrint(c)+" is not reassigneable");
            }
            if(!isAssigneableTo(propertyType, checkTarget, this.p.prettyPrint(p))) {
                typeMismatch(propertyType, checkTarget, this.p.prettyPrint(p));
            }           
        } else {
            error("["+p.name().line()+"] Type "+this.p.prettyPrint(objectType)+" has no Properties");
        }
        return null;
    }
    @Override
    public Void visitVariableSetter(VariableSetter s) {
        var variableType = getTypeOf(s.name());
        var reassigneable = isReassignable(s.name().lexeme());
        if(!reassigneable) {
            error("["+s.name().line()+"]: Cannot reassign variable "+s.name().lexeme()+" because it is immutable");
        }
        if(!isAssigneableTo(variableType, checkTarget, p.prettyPrint(s))) {
            typeMismatch(variableType, checkTarget, p.prettyPrint(s));
        }
        return null;
    }
    @Override
    public Void visitInstExpression(InstExpression i) {
        var instantiated = inferType(i.instantiated());
        var resultingType = applyTo(
                instantiated, 
                i.args().stream()
                    .map(tcomp::compileType)
                    .map(t -> env.normalize(t, this))
                    .toList());
        hasType(
            resultingType, 
            "instantiation expression " + p.prettyPrint(i));
        return null;
    }
    TypeRep applyTo(TypeRep t, List<TypeRep> args) {
        TypeFunction tf;
        if(t instanceof GenericType g) {
            tf = g.t();
        } else if(t instanceof TypeFunction f) {
            tf = f;
        } else {
            error("Tried to apply non-generic type "+p.prettyPrint(t)+" to type args "+
                args.stream().map(p::prettyPrint).toList().toString());
            return Typechecker.unknown();
        }
        return ta.apply(tf, args, false);
    }
}
