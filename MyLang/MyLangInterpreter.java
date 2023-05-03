package MyLang;

import static MyLang.MyLangAST.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MyLangInterpreter implements ExpressionVisitor<Object>,
       DeclarationVisitor<Void>, StatementVisitor<Void>, ParameterVisitor<List<Object>>, 
       PatternVisitor<Boolean>, SetterVisitor<Void> {

    public MyLangModule currentModule = new MyLangModule();

    private MyLangRunner runner;

    public Scanner inScanner = new Scanner(System.in);
    public Random random = new Random();

    boolean inConstructor = false;
    boolean isMutable = false;
    Object currentMatcher = null;

    private Map<String, MyLangCallable> listMethods = Map.ofEntries(
        Map.entry("push", MyLangBuiltinFunction.listPush),
        Map.entry("pop", MyLangBuiltinFunction.listPop),
        Map.entry("dequeue", MyLangBuiltinFunction.listDequeue),
        Map.entry("peek", MyLangBuiltinFunction.listPeek),
        Map.entry("peekLast", MyLangBuiltinFunction.listPeekLast),
        Map.entry("prepend", MyLangBuiltinFunction.listPrepend),
        Map.entry("append", MyLangBuiltinFunction.listAppend)
    );
    private MyLangClass listClass = new MyLangClass("List", listMethods, List.of(), null, new MyLangEnviroment());

    private boolean exportCurrentPatterns;
    

    MyLangEnviroment env = currentModule.names;

    public MyLangInterpreter() {
        loadBuiltins();
    }

    static final MyLangBuiltinFunction[] builtins = new MyLangBuiltinFunction[] {
        MyLangBuiltinFunction.print,
        MyLangBuiltinFunction.input,
        MyLangBuiltinFunction.number,
        MyLangBuiltinFunction.random,
        MyLangBuiltinFunction.clock,
        MyLangBuiltinFunction.openFile,
        MyLangBuiltinFunction.isNull,
        MyLangBuiltinFunction.split,
        MyLangBuiltinFunction.strip,
        MyLangBuiltinFunction.len
    };

    private void loadBuiltins() {
        for(MyLangBuiltinFunction builtin : builtins) {
            env.declareVariable(builtin.name, builtin, false);
        }
    }

    public Object interpretAny(MyLangAST ast) {
        if(ast instanceof Expression expr) {
            return interpretExpression(expr);
        } else if(ast instanceof Declaration decl) {
            interpretDeclaration(decl);
        } else if(ast instanceof Statement stmt) {
            interpretStatement(stmt);
        }
        return null;
    }

    public void interpretFile(MyLangRunner r, MyLangFile file, boolean isMainFile) {
        this.runner = r;
        for(var i: file.imports()) {
            visitImport(i);
        }
        for(Declaration decl: file.declarations()) {
            interpretDeclaration(decl);
        }

        if(isMainFile) {
            ((MyLangFunction) env.getVariable("main")).call(this, List.of(), Map.of());
        }
    }

    public void interpretStatement(Statement statement) {
        statement.accept(this);
    }
    public void interpretDeclaration(Declaration declaration) {
        declaration.accept(this);
    }

    public Object interpretExpression(Expression expression) {
        return expression.accept(this);
    }

    public List<Object> interpretParameter(Parameter parameter) {
        return parameter.accept(this);
    }

    private void openScope() {
        env = env.openScope();
    }

    private void closeScope() {
        env = env.closeScope();
    }

    public String stringify(Object obj) {
        if(obj == null) {
            return "null";
        } else if(obj instanceof Double d) {
            if(d % 1 == 0) {
                return ((int) (double) d) + "";
            } else {
                return d.toString();
            }
        } else {
            return obj.toString();
        }
    }

    @Override
    public Object visitNumericLiteral(NumericLiteral value) {
        return value.value();    
    }

    @Override
    public Object visitStringLiteral(StringLiteral value) {
        return value.value();
    }

    @Override
    public Object visitBooleanLiteral(BooleanLiteral value) {
        return value.value();
    }

    @Override
    public Object visitNullLiteral(NullLiteral value) {
        return null;
    }

    @Override
    public Object visitIdentifier(Identifier value) {
        return env.getVariable(value.value().lexeme());
    }

    public boolean truthy(Object object) {
        if(object == null) {
            return false;
        }
        if(object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    @Override
    public Object visitBinaryOperation(BinaryOperation value) {
        if(value.operator().type().shortCircuits()) {
            return switch(value.operator().type()) {
                case AND -> {
                    var left = interpretExpression(value.left());
                    if(truthy(left)) yield interpretExpression(value.right());
                    else yield left;
                }
                case OR -> {
                    var left = interpretExpression(value.left());
                    if(truthy(left)) yield left;
                    else yield interpretExpression(value.right());
                }
                default -> throw new InterpreterError("Unknown short-circuit operator: " + value.operator().type());
            };
        }
        var left = value.left().accept(this);
        var right = value.right().accept(this);
        return switch(value.operator().type()) {
            case PLUS -> {
                if (left instanceof Double l1 && right instanceof Double l2) {
                    yield l1 + l2;
                } else if (left instanceof String s1) {
                    yield s1 + stringify(right);
                } else if (right instanceof String s2) {
                    yield stringify(left) + s2;
                } else {
                    throw new InterpreterError("Unsupported operand types for +: " + left.getClass() + ", " + right.getClass());
                }
            }
            case MINUS -> (double) left - (double) right;
            case STAR -> {
                if(left instanceof Double l1 && right instanceof Double l2) {
                    yield l1 * l2;
                } else {
                    throw new InterpreterError("Unsupported operands for *: "+left+", "+right);
                }
             }
            case SLASH -> (double) left / (double) right;
            case PERCENT -> (double) left % (double) right;
            case EXPO -> Math.pow((double) left, (double) right);
            case EQUAL -> left.equals(right);
            case NOT_EQUAL -> !left.equals(right);
            case GREATER -> (double) left > (double) right;
            case GREATER_EQUAL -> (double) left >= (double) right;
            case LESS -> (double) left < (double) right;
            case LESS_EQUAL -> (double) left <= (double) right;
            case XOR -> truthy(left) ^ truthy(right);
            case IN -> contains(left, right);
            default -> throw new InterpreterError("Unimplemented operator: " + value.operator().type());
        };
    }

    private boolean contains(Object left, Object right) {
        if(right instanceof List<?> r) {
            return r.contains(left);
        } else if(right instanceof String s) {
            return s.contains((String) left);
        } else {
            throw new InterpreterError("Cannot check membership for type: " + right.getClass());
        }
    }

    @Override
    public Object visitUnaryOperation(UnaryOperation value) {
        var operand = value.operand().accept(this);
        return switch(value.operator().type()) {
            case BANG -> !(boolean) operand;
            case MINUS -> -(double) operand;
            case PLUS -> +(double) operand;
            default -> throw new InterpreterError("Unimplemented operator: " + value.operator().type());
        };
    }

    @Override
    public Object visitFunctionCall(FunctionCall value) {
        var function = interpretExpression(value.callee());
        if(function instanceof MyLangCallable theFunction) {
            List<Object> arguments = new ArrayList<>();
            for (Parameter argument : value.arguments()) {
                arguments.addAll(interpretParameter(argument));
            }
            Map<String, Object> namedArgs = new HashMap<>();
            value.named().forEach((var name, var param) -> {
                namedArgs.put(name, interpretExpression(param));
            });
            return theFunction.call(this, arguments, namedArgs);
        } else {
            throw new InterpreterError("Cannot call non-Function: " + function.getClass());
        }
    }

    @Override
    public Object visitFunctionExpression(FunctionExpression value) {
        return new MyLangFunction(
                value.optionalName(), 
                value.parameters().names(), 
                value.parameters().varargsName(), 
                value.parameters().optionals(),
                value.parameters().optionalNamed(),
                env, 
                value.body());
    }

    @Override
    public Object visitIfExpression(IfExpression value) {
        if(truthy(interpretExpression(value.condition()))) {
            return interpretExpression(value.thenBranch());
        } else {
            return interpretExpression(value.elseBranch());
        }
    }
    @Override
    public Object visitIfValExpression(IfValExpression value) {
        var matched = interpretExpression(value.matched());
        openScope();
        Object result;
        if(matches(matched, value.pat())) {
            result = interpretExpression(value.thenBranch());
        } else {
            result = interpretExpression(value.elseBranch());
        }
        closeScope();
        return result;
    }

    @Override
    public Object visitListExpression(ListExpression value) {
        List<Object> elements = new ArrayList<>();
        for(Parameter element : value.elements()) {
            elements.addAll(interpretParameter(element));
        }
        return elements;
    }

    @Override
    public Object visitIndexExpression(IndexExpression value) {
        var list = interpretExpression(value.list());
        if(list instanceof List theList) {
            var index = (double) interpretExpression(value.index());
            if(!(index >= 0 && index < theList.size()) || index % 1 != 0) {
                throw new InterpreterError("Index out of bounds or invalid index: " + stringify(index));
            }
            return theList.get((int) index);
        } else { // invalid list type
            throw new InterpreterError("Invalid list type: " + list.getClass());
        }
    }

    @Override
    public Object visitPropertyExpression(PropertyExpression value) {
        var object = interpretExpression(value.object());
        if(object instanceof MyLangObject theObject) {
            return theObject.getField(value.name().lexeme());
        } else if(object instanceof List theList) {
            if(value.name().lexeme().equals("length")) {
                return (double) theList.size();
            } else if(value.name().lexeme().equals("first")) {
                return theList.get(0);
            } else if(value.name().lexeme().equals("last")) {
                return theList.get(theList.size() - 1);
            } else if(value.name().lexeme().equals("rest")) {
                return theList.subList(1, theList.size());
            } else if(value.name().lexeme().equals("firsts")) {
                return theList.subList(0, theList.size() - 1);
            } else if(listClass.methods().containsKey(value.name().lexeme())){
                return listClass.methods().get(value.name().lexeme()).bind(theList);
            } else {
                throw new InterpreterError("List has no property '"+ value.name().lexeme()+"'");
            }
        } else if(object instanceof EnumVariantObject e) {
            return e.getProperty(value.name().lexeme());
        } else if(object instanceof MyLangModule m) {
            if(m.exports.contains(value.name().lexeme())) {
                return m.names.getVariable(value.name().lexeme());
            } else {
                throw new InterpreterError("Module '"+m.name.toString()+"' does not export '"+value.name().lexeme()+"'");
            }
        } else {
            throw new InterpreterError("Invalid object type: " + object.getClass());
        }
    }

    @Override
    public Object visitBlockExpression(BlockExpression value) {
        openScope();
        for(DeclarationOrStatement statement : value.statements()) {
            interpretAny(statement);
        }
        var result = interpretExpression(value.returnValue());
        closeScope();
        return result;
    
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatement value) {
        interpretExpression(value.expression());
        return null;
    }

    @Override
    public Void visitSetStatement(SetStatement value) {
        var setValue = interpretExpression(value.expression());
        set(value.setter(), setValue);
        return null;
    }
    private void set(Setter s, Object setValue) {
        var previousMatcher = currentMatcher;
        currentMatcher = setValue;
        s.accept(this);
        currentMatcher = previousMatcher;
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclaration value) {
        Object result = interpretExpression(value.initializer());
        var previousExport = exportCurrentPatterns;
        exportCurrentPatterns = value.export();
        matches(result, value.pat(), value.isReassignable());
        exportCurrentPatterns = previousExport;
        return null;
    }
    @Override
    public Void visitValElseDeclaration(ValElseDeclaration value) {
        Object initializer = interpretExpression(value.initializer());
        if(!matches(initializer, value.pat())) {
            interpretExpression(value.elseBranch());
        }
        return null;
    } 

    @Override
    public Void visitFunctionDeclaration(FunctionDeclaration value) {
        MyLangFunction function = new MyLangFunction(
                value.Name().lexeme(),
                value.parameters().names(), 
                value.parameters().varargsName(), 
                value.parameters().optionals(),
                value.parameters().optionalNamed(),
                env, 
                value.body());
        env.declareVariable(value.Name().lexeme(), function, false);
        if(value.export()) {
            if(currentModule.names != env) {
                throw new InterpreterError("Cannot export local variable ('"+value.Name().lexeme()+"')");
            }
            currentModule.exports.add(value.Name().lexeme());
        }
        return null;
    }
    @Override
    public Object visitWhileYieldExpression(WhileYieldExpression value) {
        List<Object> results = new ArrayList<>();
        while(truthy(interpretExpression(value.condition()))) {
            results.addAll(interpretParameter(value.body()));
        }
        return results;
    }
    @Override
    public Object visitWhileValYieldExpression(WhileValYieldExpression value) {
        List<Object> results = new ArrayList<>();
        while(true) {
            openScope();
            var matches = matches(interpretExpression(value.matched()), value.pattern());
            if(matches) {
                results.add(interpretExpression(value.body()));
                closeScope();
            } else {
                closeScope();
                break;
            }
        }

        return results; 
    }

    @Override
    public Object visitWhileDoExpression(WhileDoExpression value) {
        while(truthy(interpretExpression(value.condition()))) {
            interpretStatement(value.body());
        }
        return null;
    }
    @Override
    public Object visitWhileValDoExpression(WhileValDoExpression value) {
        while(true) {
            openScope();
            var matches = matches(interpretExpression(value.matched()), value.pattern());
            if(matches) {
                interpretExpression(value.body());
                closeScope();
            } else {
                closeScope();
                break;
            }
        }
        return null;
    }

    @Override
    public Object visitForYieldExpression(ForYieldExpression value) {
        List<Object> results = new ArrayList<>();
        var collection = interpretExpression(value.collection());
        if(collection instanceof List theList) {
            for(Object element : theList) {
                openScope();
                if(matches(element, value.pat()) && truthy(interpretExpression(value.guard()))) {
                    results.addAll(interpretParameter(value.body()));
                }
                closeScope();
            }
            return results;
        } else {
            throw new InterpreterError("Invalid list type: " + collection.getClass());
        }
    }

    @Override
    public Object visitForDoExpression(ForDoExpression value) {
        var collection = interpretExpression(value.collection());
        if(collection instanceof List theList) {
            for(Object element : theList) {
                openScope();
                if(matches(element, value.pat()) && truthy(interpretExpression(value.guard()))) {
                    interpretStatement(value.body());
                }
                closeScope();
            }
            return null;
        } else {
            throw new InterpreterError("Invalid list type: " + collection.getClass());
        }
    }

    @Override
    public Object visitRangeExpression(RangeExpression value) {
        var start = interpretExpression(value.start());
        var step = interpretExpression(value.step());
        var end = interpretExpression(value.end());
        var resultingList = new ArrayList<Double>();
        if(start instanceof Double s && step instanceof Double c && end instanceof Double e) {
            for(double i = s; i < e; i += c) {
                resultingList.add(i);
            }
            return resultingList;
        } else {
            throw new InterpreterError("Invalid Types for Range expression");
        }
    }

    @Override
    public Void visitClassDeclaration(ClassDeclaration value) {
        env = env.openScope();
        var constructor = value.constructor() != null ? 
            compileConstructorToMethod(value.Name().lexeme(), value.constructor()) : null;
        var fields = compileFields(value.fieldsAndMethods());
        Map<String, MyLangCallable> methods = compileMethods(value.fieldsAndMethods());
        env = env.closeScope();
        var result = new MyLangClass(value.Name().lexeme(),
                                     methods,
                                     fields,
                                     constructor,
                                     env);
        env.declareVariable(value.Name().lexeme(), result, false);
        if(value.export()) {
            if(currentModule.names != env) {
                throw new InterpreterError("Cannot export local variable ('"+value.Name().lexeme()+"')");
            }
            currentModule.exports.add(value.Name().lexeme());
        }
        return null;
    }

    private List<VariableDeclaration> compileFields(List<Declaration> fieldsAndMethods) {
        return fieldsAndMethods.stream()
            .filter((var fieldOrMethod) -> (fieldOrMethod instanceof VariableDeclaration))
            .map(field -> (VariableDeclaration) field)
            .toList();
    }

    private Map<String, MyLangCallable> compileMethods(List<? extends Declaration> fieldsAndMethods) {
        return fieldsAndMethods.stream()
            .filter((var fieldOrMethod) -> (fieldOrMethod instanceof FunctionDeclaration))
            .map(field -> (FunctionDeclaration) field)
            .map((FunctionDeclaration declaration) -> {
                declaration.parameters().names().add(0, new Token(TokenType.VALUE_THIS, "this", declaration.Name().line()));
                return new MyLangFunction(
                        declaration.Name().lexeme(),
                        declaration.parameters().names(),
                        declaration.parameters().varargsName(),
                        declaration.parameters().optionals(),
                        declaration.parameters().optionalNamed(),
                        env,
                        declaration.body());
            }).collect(Collectors.toMap((MyLangCallable method) -> (method.getName()),
                    (MyLangCallable method) -> (method)));
    }

    private MyLangFunction compileConstructorToMethod(String className, ClassConstructor constructor) {
        constructor.parameters().names().add(0, new Token(TokenType.VALUE_THIS, "this", constructor.keyword().line()));
        return new MyLangFunction(
                className+"Init", 
                constructor.parameters().names(),
                constructor.parameters().varargsName(),
                constructor.parameters().optionals(),
                constructor.parameters().optionalNamed(),
                env, 
                constructor.body());
    }

    @Override
    public Object visitThisExpression(ThisExpression value) {
        if(!env.isInClass()) {
            System.out.println(env);
            throw new InterpreterError("Cannot use 'this' outside of a class.");
        }
        return env.getVariable("this");
    }

    @Override
    public List<Object> visitExpressionParameter(ExpressionParameter value) {
        List<Object> list = new ArrayList<>();
        list.add(interpretExpression(value.expr()));
        return list;
    }

    @Override
    public List<Object> visitSpreadParameter(SpreadParameter value) {
        var result = interpretExpression(value.collection());
        if(result instanceof List list) {
            return list;
        } else {
            throw new InterpreterError("Invalid type for spread operator: "+result.getClass());
        }
    }

    @Override
    public List<Object> visitConditionalParameter(ConditionalParameter value) {
        if(truthy(interpretExpression(value.guard()))) {
            List<Object> list = new ArrayList<>();
            list.add(interpretExpression(value.body()));
            return list;
        } else {
            return List.of();
        }
    }

    @Override
    public Void visitModuleDeclaration(ModuleDeclaration value) {
        if(currentModule.name != null) {
            throw new InterpreterError("["+value.Name().names().get(0).line()+"]: Module already declared: "+
                    currentModule.name);
        } else {
            currentModule.name = value.Name();
            env.declareVariable(currentModule.name.toString(), currentModule, false);
        }
        return null;
    }

    public Void visitImport(Import value) {
        var v = (ImportDeclaration) value;
        var resolvedPath = runner.resolvePath(v.Name());
        if(runner.interpretedFiles.containsKey(resolvedPath)) {
            env.declareModule(v.Name(), runner.interpretedFiles.get(resolvedPath));
        } else {
            var code = runner.compiledFiles.get(resolvedPath);
            var interpreter = new MyLangInterpreter();
            interpreter.interpretFile(runner, code, false);
            runner.interpretedFiles.put(resolvedPath, interpreter.currentModule);
            env.declareModule(v.Name(), interpreter.currentModule);
        }
        return null;
    }
    
    @Override
    public Void visitEmptyStatement(EmptyStatement value) {
        return null;
    }
    @Override public Void visitEmptyDeclaration(EmptyDeclaration value) {
        return null;
    }
    @Override
    public Void visitTypeDefDeclaration(TypeDefDeclaration t) {
        return null;
    }


    @Override
    public Void visitIfStatement(IfStatement i) {
        if(truthy(interpretExpression(i.condition()))) {
            interpretStatement(i.body());
        }
        return null;
    } 
    @Override
    public Void visitReturnExpression(ReturnExpression r) {
        throw new ReturnException(interpretExpression(r.returnValue()));
    }
    @Override
    public List<Object> visitNamedParameter(NamedParameter n) {
        throw new InterpreterError("Unexpected named parameter: "+n);
    }
    @Override
    public Void visitEnumDeclaration(EnumDeclaration e) {
        Map<String, MyLangCallable> methods = compileMethods(e.methods());
        for(var variant: e.variants()) {
            declareEnumConstructor(variant, methods);
        }
        return null;
    }

    private void declareEnumConstructor(EnumConstructor e, Map<String, MyLangCallable> methods) {
        env.declareVariable(e.name().lexeme(), new EnumVariant(e.name(), e.parameters().size(), methods), false);
    }

    @Override
    public Object visitMatchExpression(MatchExpression m) {
        var matched = interpretExpression(m.matched());
        for(int i = 0; i < m.cases().size(); i++) {
            openScope();
            if(matches(matched, m.cases().get(i))) {
                var result = interpretExpression(m.branches().get(i));
                closeScope();
                return result;
            }
            closeScope();
        }

        throw new InterpreterError("Non exhaustive match while matching "+matched.toString());
    }

    private boolean matches(Object o, Pattern p) {
        return matches(o, p, false);
    }

    private boolean matches(Object o, Pattern p, boolean isReassigneable) {
        var previousMatcher = currentMatcher;
        currentMatcher = o;
        var previousMutable = isMutable;
        isMutable = isReassigneable;
        var result = p.accept(this);
        isMutable = previousMutable;
        currentMatcher = previousMatcher;
        return result;
    }

    @Override
    public Boolean visitWildcard(Wildcard w) {
        return true;
    }
    @Override
    public Boolean visitVariableBinding(VariableBinding v) {
        env.declareVariable(v.name().lexeme(), currentMatcher, isMutable);

        if(exportCurrentPatterns) {
            if(currentModule.names != env) {
                throw new InterpreterError("Cannot export local variable "+v.name().lexeme());
            }
            currentModule.exports.add(v.name().lexeme());
        }
        return true;
    }
    @Override
    public Boolean visitStringPattern(StringPattern s) {
        return s.value().equals(currentMatcher);
    }
    @Override
    public Boolean visitNumberPattern(NumberPattern n) {
        return currentMatcher.equals(n.value());
    }
    @Override
    public Boolean visitBooleanPattern(BooleanPattern b) {
        return currentMatcher.equals(b.value());
    }
    @Override
    public Boolean visitConstructorPattern(ConstructorPattern p) {
        if(currentMatcher instanceof EnumVariantObject o) {
            if(p.constr().lexeme().equals(o.variant().Name().lexeme())) {
                for(int i = 0; i < p.subPatterns().size(); i++) {
                    if(!matches(o.fields().get(i), p.subPatterns().get(i))) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    @Override
    public Object visitTupleExpression(TupleExpression t) {
        var exprs = t.elements();
        List<Object> resulting = new ArrayList<>();
        for(var expr: exprs) {
            resulting.add(interpretExpression(expr));
        }
        return resulting.toArray();
    }
    @Override
    public Boolean visitTuplePattern(TuplePattern tp) {
        if(currentMatcher instanceof Object[] tuple) {
            if(tuple.length != tp.subPatterns().size()) {
                return false;
            }
            boolean allMatch = true;
            for(int i = 0; i < tuple.length; i++) {
                allMatch &= matches(tuple[i], tp.subPatterns().get(i), isMutable);
            }
            return allMatch;
        } else {
            return false;
        }
    }
    @Override
    public Void visitWildcardExpression(WildcardExpression w) {
        throw new InterpreterError("Reached Wildcard on line "+w.position().line());
    }
    @Override
    public Void visitWildcardSetter(WildcardSetter w) {
        return null;
    }
    @Override
    public Void visitVariableSetter(VariableSetter s) {
        if(!env.localVariableDeclared(s.name().lexeme())) {
            System.out.println(env);
        }
        env.setVariable(s.name().lexeme(), currentMatcher);
        return null;
    }
    @Override
    public Void visitTupleSetter(TupleSetter t) {
        var tuple = (Object[]) currentMatcher;
        for(int i = 0; i < tuple.length; i++) {
            set(t.setters().get(i), tuple[i]);
        }
        return null;
    }
    @Override
    public Void visitIndexSetter(IndexSetter i) {
        var list = (List) interpretExpression(i.list());
        var index = (Double) interpretExpression(i.index());
        if(index % 1 != 0) {
            throw new InterpreterError("Invalid index "+index);
        }
        if(index < 0 || index >= list.size()) {
            throw new InterpreterError("Index "+index+" out of range for list of length "+list.size());
        }
        list.set((int) (double) index, currentMatcher);
        return null;
    }
    @Override
    public Void visitPropertySetter(PropertySetter p) {
        var object = (MyLangObject) interpretExpression(p.object());
        object.setField(p.name().lexeme(), currentMatcher, inConstructor);
        return null;
    }
}
