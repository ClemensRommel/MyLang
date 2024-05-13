package MyLang;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import static MyLang.MyLangAST.*;
import java.nio.file.*;
import java.io.IOException;

public abstract class MyLangBuiltinFunction implements MyLangCallable {
    public final String name;
    public final TypeRep type;

    protected MyLangBuiltinFunction(String name, TypeRep t) {
        this.name = name; type = t;
    }

    protected void noNamedArgs(Map<String, Object> named, MyLangInterpreter interpreter) {
        if(!named.isEmpty()) {
            throw new InterpreterError("Builtin function '"+this.name+"' does not take named arguments", interpreter.callStack);
        }
    }

    public static final MyLangBuiltinFunction len = new MyLangBuiltinFunction("len",
        new FunctionTypeRep(
            List.of(Typechecker.stringType),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.numberType,
            new TypeEnv()
        )) {
        @Override
        public Object call(MyLangInterpreter i, List<Object> args, Map<String, Object> named) {
            return (double) ((String) args.get(0)).length();
        }
    };

    public static final MyLangBuiltinFunction args = new MyLangBuiltinFunction("args", 
        new FunctionTypeRep(
            List.of(),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            new ListOfRep(Typechecker.stringType),
            new TypeEnv()
        )) {

            @Override
            public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
                return interpreter.program_args;
            }
        };

    public static final MyLangBuiltinFunction matches = new MyLangBuiltinFunction("matches",
        new FunctionTypeRep(
            List.of(Typechecker.stringType, Typechecker.stringType),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.booleanType,
            new TypeEnv()
        )) {
            @Override
            public Object call(MyLangInterpreter interpreter, List<Object> posArgs, Map<String, Object> namedArgs) {
                return ((String) posArgs.get(0)).matches((String) posArgs.get(1));
            }

        };

    public static final MyLangBuiltinFunction panic = new MyLangBuiltinFunction("panic",
        new FunctionTypeRep(
            List.of(Typechecker.stringType),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.neverType,
            new TypeEnv()
        )) {

            @Override
            public Object call(MyLangInterpreter interpreter, List<Object> posArgs, Map<String, Object> namedArgs) {
                System.out.println(posArgs.get(0));
                System.out.println("Panicked at:");
                while(!interpreter.callStack.isEmpty()) {
                    System.out.print("  ");
                    var element = interpreter.callStack.pop();
                    System.out.println(element.name());
                }
                System.exit(0);
                return null;
            }

        };

    public static final MyLangBuiltinFunction replace = new MyLangBuiltinFunction("replace",
        new FunctionTypeRep(
            List.of(Typechecker.stringType, Typechecker.stringType, Typechecker.stringType),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.stringType,
            new TypeEnv()
        )) {
            @Override
            public Object call(MyLangInterpreter interpreter, List<Object> posArgs, Map<String, Object> namedArgs) {
                return ((String) posArgs.get(0)).replace((String) posArgs.get(1), (String) posArgs.get(2));
            }
            
    };
    public static final MyLangBuiltinFunction split = new MyLangBuiltinFunction("split",
        new FunctionTypeRep(
            List.of(Typechecker.stringType, Typechecker.stringType),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            new ListOfRep(Typechecker.stringType),
            new TypeEnv()
        )) {
    @Override
    public Object call(MyLangInterpreter i, List<Object> args, Map<String, Object> named) {
            var toBeSplitted = (String) args.get(0);
            var regex = (String) args.get(1);
            var results = toBeSplitted.split(regex);
            var result = new ArrayList<Object>(results.length);
            for(var r: results) {
                result.add(r);
            }
            return result;
        }
};

    public static final MyLangBuiltinFunction join = new MyLangBuiltinFunction("join",
        new FunctionTypeRep(
            List.of(Typechecker.stringType, new ListOfRep(Typechecker.stringType)),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.stringType,
            new TypeEnv()
        )) {
            @Override
            public Object call(MyLangInterpreter i, List<Object> args, Map<String, Object> named) {
                String separator =  (String) args.get(0);
                ArrayList list = (ArrayList) args.get(1);
                return String.join(separator, list);
            }
        };

    public static final MyLangBuiltinFunction strip = new MyLangBuiltinFunction("strip", 
        new FunctionTypeRep(
            List.of(Typechecker.stringType),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.stringType,
            new TypeEnv()
        )) {
        @Override
        public Object call(MyLangInterpreter i, List<Object> args, Map<String, Object> named) {
            return ((String) args.get(0)).strip();
        }
    };

    public static final MyLangBuiltinFunction isNull = new MyLangBuiltinFunction("isNull",
        new FunctionTypeRep(
            List.of(Typechecker.voidType),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.booleanType, new TypeEnv()
        )) {
        @Override
        public Object call(MyLangInterpreter i, List<Object> args, Map<String, Object> named) {
            return args.get(0) == null;
        }
    };

    public static final MyLangBuiltinFunction openFile = new MyLangBuiltinFunction(
        "openFile",
        new FunctionTypeRep(
            List.of(Typechecker.stringType),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.stringType,
            new TypeEnv()
        )) {
            @Override
            public Object call(
                MyLangInterpreter interpreter, 
                List<Object> args, 
                Map<String, Object> named) {
                try {
                    return Files.readString(Paths.get((String) args.get(0)));
                } catch(IOException e) {
                    return null;
                }
            }
    };

    public static final MyLangBuiltinFunction writeToFile = new MyLangBuiltinFunction(
        "writeToFile",
        new FunctionTypeRep(
            List.of(Typechecker.stringType, Typechecker.stringType),
            List.of(),
            Map.of(),
            Map.of(),
            null,
            Typechecker.voidType, new TypeEnv())
        ) {

            @Override
            public Object call(MyLangInterpreter interpreter, List<Object> posArgs, Map<String, Object> namedArgs) {
                noNamedArgs(namedArgs, interpreter);
                var path = Paths.get((String) posArgs.get(0));
                try {
                    Files.writeString(path, (String) posArgs.get(1));
                } catch (IOException e) {
                    panic.call(interpreter, List.of("Could not write to file: "+posArgs.get(0)), Map.of());
                }
                return null;
            }

        };

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
            noNamedArgs(named, interpreter);
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
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
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
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
            if(args.get(0) instanceof String s) {
                return Double.parseDouble(s);
            } else {
                throw new InterpreterError("Expected a String, got " + args.get(0).getClass() + " calling function '"+name+"'", interpreter.callStack);
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
                throw new InterpreterError("Expected 2 arguments, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
            if(args.get(0) instanceof List l) {
                l.add(args.get(1));
            } else {
                throw new InterpreterError("Invalid this target for list push", interpreter.callStack);
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
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
            if(args.get(0) instanceof List l) {
                if(l.isEmpty()) {
                    throw new InterpreterError("Cannot pop from empty List", interpreter.callStack);
                }
                return l.remove(l.size() - 1);
            } else {
                throw new InterpreterError("Invalid this target for list pop", interpreter.callStack);
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
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
            if(args.get(0) instanceof List l) {
                if(l.isEmpty()) {
                    throw new InterpreterError("Cannot dequeue from empty List", interpreter.callStack);
                }
                return l.remove(0);
            } else {
                throw new InterpreterError("Invalid this target for list dequeue", interpreter.callStack);
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
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
            if(args.get(0) instanceof List l) {
                if(l.isEmpty()) {
                    throw new InterpreterError("Cannot peek from empty List", interpreter.callStack);
                }
                return l.get(0);
            } else {
                throw new InterpreterError("Invalid this target for list peek", interpreter.callStack);
            }
        }
    };

    public static final MyLangBuiltinFunction listPeekLast = new MyLangBuiltinFunction(
            "peekLast",
            new FunctionTypeRep(List.of(),List.of(), Map.of(), Map.of(), null, Typechecker.voidType, new TypeEnv())) {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
            if(args.get(0) instanceof List l) {
                if(l.isEmpty()) {
                    throw new InterpreterError("Cannot peekLast from empty List", interpreter.callStack);
                }
                return l.get(l.size() - 1);
            } else {
                throw new InterpreterError("Invalid this target for list peekLast", interpreter.callStack);
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
                throw new InterpreterError("Expected 2 arguments, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
            if(args.get(0) instanceof List l) {
                l.add(0, args.get(1));
            } else {
                throw new InterpreterError("Invalid this target for list prepend", interpreter.callStack);
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
                throw new InterpreterError("Expected 2 arguments, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
            if(args.get(0) instanceof List l) {
                l.addAll((List<?>) args.get(1));
            } else {
                throw new InterpreterError("Invalid this target for list append", interpreter.callStack);
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
                throw new InterpreterError("Expected 0 arguments, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
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
                throw new InterpreterError("Expected 0 arguments, got " + args.size() + " calling function '"+name+"'", interpreter.callStack);
            }
            noNamedArgs(named, interpreter);
            return (double) System.currentTimeMillis();
        }
    };

    public String getName() {
        return name;
    }
    public String getFileName() {
        return "builtins";
    }

    @Override
    public String toString() {
        return "<builtin function '" + name + "'>";
    }

    
}
