package MyLang;

import static MyLang.MyLangAST.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MyLangInterpreter implements ExpressionVisitor<Object>, DeclarationVisitor<Void>, StatementVisitor<Void>, ParameterVisitor<List<Object>> {

    public Scanner inScanner = new Scanner(System.in);
    public Random random = new Random();

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
    

    MyLangEnviroment env = new MyLangEnviroment();

    public MyLangInterpreter() {
        loadBuiltins();
    }

    private static final MyLangBuiltinFunction[] builtins = new MyLangBuiltinFunction[] {
        MyLangBuiltinFunction.print,
        MyLangBuiltinFunction.input,
        MyLangBuiltinFunction.number,
        MyLangBuiltinFunction.random,
        MyLangBuiltinFunction.clock
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

    public void interpretProgram(MyLangProgram program) {
        for(var stmt: program.stmts()) {
            interpretDeclarationOrStatement(stmt);
        }
    }

    private void interpretDeclarationOrStatement(DeclarationOrStatement stmtOrDecl) {
        if(stmtOrDecl instanceof Declaration decl) {
            interpretDeclaration(decl);
        } else if(stmtOrDecl instanceof Statement stmt) {
            interpretStatement(stmt);
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
            case STAR -> (double) left * (double) right;
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
        } else if(right instanceof MyLangRange r) {
            double l = (double) left;
            return l >= r.start() && l < r.end() && (l - r.start()) % r.step() == 0;
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
            return theFunction.call(this, arguments);
        } else {
            throw new InterpreterError("Cannot call non-Function: " + function.getClass());
        }
    }

    @Override
    public Object visitFunctionExpression(FunctionExpression value) {
        return new MyLangFunction(value.optionalName(), value.parameters(), value.varargsName(), env, value.body());
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
        } else if(list instanceof MyLangRange range) {
            var index = (double) interpretExpression(value.index());
            if(index % 1 != 0) {
                throw new InterpreterError("Index out of bounds or invalid index: " + stringify(index));
            }

            var wouldBeValue = range.start() + range.step() * index;
            if(wouldBeValue < range.start() || wouldBeValue > range.end()) {
                throw new InterpreterError("Index out of bounds or invalid index: " + stringify(index));
            }
            return wouldBeValue;
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
                return theList.size();
            } else if(value.name().lexeme().equals("first")) {
                return theList.get(0);
            } else if(value.name().lexeme().equals("last")) {
                return theList.get(theList.size() - 1);
            } else if(value.name().lexeme().equals("rest")) {
                return theList.subList(1, theList.size());
            } else if(value.name().lexeme().equals("head")) {
                return theList.subList(0, theList.size() - 1);
            } else if(listClass.methods().containsKey(value.name().lexeme())){
                return listClass.methods().get(value.name().lexeme()).bind(theList);
            } else {
                throw new InterpreterError("List has no property "+ value.name().lexeme());
            }
        } else if(object instanceof MyLangRange r) {
            if(value.name().lexeme().equals("asList")) {
                return MyLangBuiltinFunction.RangeAsList.bind(r);
            } else {
                throw new InterpreterError("Range has no property "+ value.name().lexeme());
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
        env.setVariable(value.name().lexeme(), interpretExpression(value.expression()));
        return null;
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclaration value) {
        env.declareVariable(value.Name().lexeme(), interpretExpression(value.initializer()), value.isReassignable());
        return null;
    }

    @Override
    public Void visitFunctionDeclaration(FunctionDeclaration value) {
        MyLangFunction function = new MyLangFunction(value.Name().lexeme(), value.parameters(), value.varargsName(), env, value.body());
        env.declareVariable(value.Name().lexeme(), function, false);
        return null;
    }

    @Override
    public Void visitSetIndexStatement(SetIndexStatement value) {
        var list = interpretExpression(value.list());
        var uncheckedIndex = interpretExpression(value.index());
        var newVal = interpretExpression(value.expression());
        if(list instanceof List theList) {
            var index = (double) uncheckedIndex;
            if(!(index >= 0 && index < theList.size()) || index % 1 != 0) {
                throw new InterpreterError("Index out of bounds or invalid index: " + stringify(index));
            }
            List<Object> actualList = theList;
            actualList.set((int) index, newVal);
            return null;
        } else if(list instanceof MyLangRange) {
            throw new InterpreterError("Cannot change values of range");
        } else {
            throw new InterpreterError("Invalid list type: " + list.getClass());
        }
    }

    @Override
    public Void visitSetPropertyStatement(SetPropertyStatement value) {
        var object = interpretExpression(value.target());
        var newValue = interpretExpression(value.expression());
        if(object instanceof MyLangObject theObject) {
            theObject.setField(value.name().lexeme(), newValue);
            return null;
        } else if(object instanceof List theList) {
            if(value.name().lexeme().equals("first")) {
                theList.set(0, newValue);
                return null;
            } else if(value.name().lexeme().equals("last")) {
                theList.set(theList.size() - 1, newValue);
                return null;
            } else if(value.name().lexeme().equals("rest") || value.name().lexeme().equals("head")) {
                throw new InterpreterError("Cannot change head or rest of List");
            } else {
                throw new InterpreterError("List has no property called "+ value.name().lexeme());
            }
        } else {
            throw new InterpreterError("Invalid object type: " + object.getClass());
        }
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
    public Object visitWhileDoExpression(WhileDoExpression value) {
        while(truthy(interpretExpression(value.condition()))) {
            interpretStatement(value.body());
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
                env.declareVariable(value.variable().lexeme(), element, false);
                if(truthy(interpretExpression(value.guard()))) {
                    results.addAll(interpretParameter(value.body()));
                }
                closeScope();
            }
            return results;
        } else if(collection instanceof MyLangRange range) {
            for(double i = range.start(); i < range.end(); i += range.step()) {
                openScope();
                env.declareVariable(value.variable().lexeme(), i, false);
                if(truthy(interpretExpression(value.guard()))) {
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
                env.declareVariable(value.variable().lexeme(), element, false);
                if(truthy(interpretExpression(value.guard()))) {
                    interpretStatement(value.body());
                }
                closeScope();
            }
            return null;
        } else if(collection instanceof MyLangRange range) {
            for(double i = range.start(); i < range.end(); i += range.step()) {
                openScope();
                env.declareVariable(value.variable().lexeme(), i, false);
                if(truthy(interpretExpression(value.guard()))) {
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
        return new MyLangRange((double) start, (double) end, (double) step);
    }

    @Override
    public Void visitClassDeclaration(ClassDeclaration value) {
        env = env.openScope();
        var constructor = value.constructor() != null ? compileConstructorToMethod(value.Name().lexeme(), value.constructor()) : null;
        var fields = compileFields(value.fieldsAndMethods());
        Map<String, MyLangCallable> methods = compileMethods(value.fieldsAndMethods());
        env.closeScope();
        env.declareVariable(value.Name().lexeme(),
            new MyLangClass(value.Name().lexeme(),
                            methods,
                            fields,
                            constructor,
                            env),
            false);

        return null;
    }

    private List<VariableDeclaration> compileFields(List<Declaration> fieldsAndMethods) {
        return fieldsAndMethods.stream()
            .filter((var fieldOrMethod) -> (fieldOrMethod instanceof VariableDeclaration))
            .map(field -> (VariableDeclaration) field)
            .toList();
    }

    private Map<String, MyLangCallable> compileMethods(List<Declaration> fieldsAndMethods) {
        return fieldsAndMethods.stream()
            .filter((var fieldOrMethod) -> (fieldOrMethod instanceof FunctionDeclaration))
            .map(field -> (FunctionDeclaration) field)
            .map((FunctionDeclaration declaration) -> {
                declaration.parameters().add(0, new Token(TokenType.VALUE_THIS, "this", declaration.Name().line()));
                return new MyLangFunction(declaration.Name().lexeme(), declaration.parameters(), declaration.varargsName(), env, declaration.body());
            }).collect(Collectors.toMap((MyLangCallable method) -> (method.getName()), (MyLangCallable method) -> (method)));
    }

    private MyLangFunction compileConstructorToMethod(String className, ClassConstructor constructor) {
        constructor.parameters().add(0, new Token(TokenType.VALUE_THIS, "this", constructor.keyword().line()));
        return new MyLangFunction(className+"Init", constructor.parameters(), constructor.varargsName(), env, constructor.body());
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
        } else if(result instanceof MyLangRange range) {
            return (List) MyLangBuiltinFunction.RangeAsList.call(this, List.of(range));
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
    
}
