package MyLang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static MyLang.MyLangAST.*;

public abstract class MyLangBuiltinFunction implements MyLangCallable {
    public final String name;
    public final TypeRep type;

    protected MyLangBuiltinFunction(String name, TypeRep t) {
        this.name = name; type = t;
    }

    protected void noNamedArgs(Map<String, Object> named) {
        if(!named.isEmpty()) {
            throw new InterpreterError("Builtin function '"+this.name+"' does not take named arguments");
        }
    }


    public static final MyLangBuiltinFunction print = new MyLangBuiltinFunction(
            "print", 
            new FunctionTypeRep(
                List.of(), 
                List.of(),
                Map.of(),
                Map.of(),
                Typechecker.voidType, 
                Typechecker.voidType, new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            noNamedArgs(named);
            for (Object object : args) {
                System.out.print(interpreter.stringify(object));
            }   
            System.out.println();
            return null;
        }
    };

    public static final MyLangBuiltinFunction input = new MyLangBuiltinFunction(
            "input",
            new FunctionTypeRep(
                List.of(Typechecker.voidType), 
                List.of(),
                Map.of(),
                Map.of(),
                null, 
                Typechecker.stringType,
            new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            System.out.print(interpreter.stringify(args.get(0)));
            return interpreter.inScanner.nextLine();
        }
    };
    
    public static final MyLangBuiltinFunction number = new MyLangBuiltinFunction(
            "number",
            new FunctionTypeRep(
                List.of(Typechecker.stringType), 
                List.of(),
                Map.of(),
                Map.of(),
                null, 
                Typechecker.numberType,
                new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            if(args.get(0) instanceof String s) {
                return Double.parseDouble(s);
            } else {
                throw new InterpreterError("Expected a String, got " + args.get(0).getClass() + " calling function '"+name+"'");
            }
        }
    };
    public static final MyLangBuiltinFunction listPush = new MyLangBuiltinFunction(
            "push",
            new FunctionTypeRep(
                List.of(Typechecker.voidType),
                List.of(),
                Map.of(),
                Map.of(),
                null, 
                Typechecker.voidType,
                new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 2) {
                throw new InterpreterError("Expected 2 arguments, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            if(args.get(0) instanceof List l) {
                l.add(args.get(1));
            } else {
                throw new InterpreterError("Invalid this target for list push");
            }
            return null;
        }  
    };

    public static final MyLangBuiltinFunction listPop = new MyLangBuiltinFunction(
            "pop",
            new FunctionTypeRep(List.of(),List.of(), Map.of(), Map.of(), null, Typechecker.voidType, new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            if(args.get(0) instanceof List l) {
                if(l.isEmpty()) {
                    throw new InterpreterError("Cannot pop from empty List");
                }
                return l.remove(l.size() - 1);
            } else {
                throw new InterpreterError("Invalid this target for list pop");
            }
        }
    };

    public static final MyLangBuiltinFunction listDequeue = new MyLangBuiltinFunction(
        "dequeue",
        new FunctionTypeRep(
            List.of(), 
            List.of(),
            Map.of(),
            Map.of(),
            null, 
            Typechecker.voidType, new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            if(args.get(0) instanceof List l) {
                if(l.isEmpty()) {
                    throw new InterpreterError("Cannot dequeue from empty List");
                }
                return l.remove(0);
            } else {
                throw new InterpreterError("Invalid this target for list dequeue");
            }
        }
    };

    public static final MyLangBuiltinFunction listPeek = new MyLangBuiltinFunction(
            "peek",
            new FunctionTypeRep(
                List.of(), 
                List.of(),
                Map.of(),
                Map.of(),
                null, 
                Typechecker.voidType, new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            if(args.get(0) instanceof List l) {
                if(l.isEmpty()) {
                    throw new InterpreterError("Cannot peek from empty List");
                }
                return l.get(0);
            } else {
                throw new InterpreterError("Invalid this target for list peek");
            }
        }
    };

    public static final MyLangBuiltinFunction listPeekLast = new MyLangBuiltinFunction(
            "peekLast",
            new FunctionTypeRep(List.of(),List.of(), Map.of(), Map.of(), null, Typechecker.voidType, new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            if(args.get(0) instanceof List l) {
                if(l.isEmpty()) {
                    throw new InterpreterError("Cannot peekLast from empty List");
                }
                return l.get(l.size() - 1);
            } else {
                throw new InterpreterError("Invalid this target for list peekLast");
            }
        }
    };

    public static final MyLangBuiltinFunction listPrepend = new MyLangBuiltinFunction(
        "prepend",
        new FunctionTypeRep(
            List.of(Typechecker.voidType),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.voidType,
            new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 2) {
                throw new InterpreterError("Expected 2 arguments, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            if(args.get(0) instanceof List l) {
                l.add(0, args.get(1));
            } else {
                throw new InterpreterError("Invalid this target for list prepend");
            }
            return null;
        }
    };

    public static final MyLangBuiltinFunction listAppend = new MyLangBuiltinFunction(
            "append",
            new FunctionTypeRep(
                List.of(Typechecker.voidType),
                List.of(),
                Map.of(),
                Map.of(),
                null,
                Typechecker.voidType,
                new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 2) {
                throw new InterpreterError("Expected 2 arguments, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            if(args.get(0) instanceof List l) {
                l.addAll((List<?>) args.get(1));
            } else {
                throw new InterpreterError("Invalid this target for list append");
            }
            return null;
        }  
    };

    public static final MyLangBuiltinFunction random = new MyLangBuiltinFunction(
            "random",
            new FunctionTypeRep(List.of(),List.of(), Map.of(), Map.of(), null, Typechecker.numberType, new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 0) {
                throw new InterpreterError("Expected 0 arguments, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            return interpreter.random.nextDouble();
        }
    };

    public static final MyLangBuiltinFunction clock = new MyLangBuiltinFunction(
        "clock",
        new FunctionTypeRep(
            List.of(),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.numberType, new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 0) {
                throw new InterpreterError("Expected 0 arguments, got " + args.size() + " calling function '"+name+"'");
            }
            noNamedArgs(named);
            return (double) System.currentTimeMillis();
        }
    };

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "<builtin function '" + name + "'>";
    }
}
