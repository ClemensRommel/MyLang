package MyLang;

import java.util.ArrayList;
import java.util.List;

public abstract class MyLangBuiltinFunction implements MyLangCallable {
    public final String name;

    protected MyLangBuiltinFunction(String name) {
        this.name = name;
    }


    public static final MyLangBuiltinFunction print = new MyLangBuiltinFunction("print") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            for (Object object : args) {
                System.out.print(interpreter.stringify(object));
            }   
            System.out.println();
            return null;
        }
    };

    public static final MyLangBuiltinFunction input = new MyLangBuiltinFunction("input") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
            System.out.print(interpreter.stringify(args.get(0)));
            return interpreter.inScanner.nextLine();
        }
    };
    
    public static final MyLangBuiltinFunction number = new MyLangBuiltinFunction("number") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
            if(args.get(0) instanceof String s) {
                return Double.parseDouble(s);
            } else {
                throw new InterpreterError("Expected a String, got " + args.get(0).getClass() + " calling function '"+name+"'");
            }
        }
    };
    public static final MyLangBuiltinFunction listPush = new MyLangBuiltinFunction("push") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 2) {
                throw new InterpreterError("Expected 2 arguments, got " + args.size() + " calling function '"+name+"'");
            }
            if(args.get(0) instanceof List l) {
                l.add(args.get(1));
            } else {
                throw new InterpreterError("Invalid this target for list push");
            }
            return null;
        }  
    };

    public static final MyLangBuiltinFunction listPop = new MyLangBuiltinFunction("pop") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
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

    public static final MyLangBuiltinFunction listDequeue = new MyLangBuiltinFunction("dequeue") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
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

    public static final MyLangBuiltinFunction listPeek = new MyLangBuiltinFunction("peek") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
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

    public static final MyLangBuiltinFunction listPeekLast = new MyLangBuiltinFunction("peekLast") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
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

    public static final MyLangBuiltinFunction listPrepend = new MyLangBuiltinFunction("prepend") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 2) {
                throw new InterpreterError("Expected 2 arguments, got " + args.size() + " calling function '"+name+"'");
            }
            if(args.get(0) instanceof List l) {
                l.add(0, args.get(1));
            } else {
                throw new InterpreterError("Invalid this target for list prepend");
            }
            return null;
        }
    };

    public static final MyLangBuiltinFunction listAppend = new MyLangBuiltinFunction("append") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 2) {
                throw new InterpreterError("Expected 2 arguments, got " + args.size() + " calling function '"+name+"'");
            }
            if(args.get(0) instanceof List l) {
                l.addAll((List<?>) args.get(1));
            } else {
                throw new InterpreterError("Invalid this target for list append");
            }
            return null;
        }  
    };

    public static final MyLangBuiltinFunction RangeAsList = new MyLangBuiltinFunction("asList") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 1) {
                throw new InterpreterError("Expected 1 argument, got " + args.size() + " calling function '"+name+"'");
            }
            if(args.get(0) instanceof MyLangRange r) {
                List<Object> list = new ArrayList<>();
                for(double i = r.start(); i < r.end(); i += r.step()) {
                    list.add(i);
                }
                return list;
            } else {
                throw new InterpreterError("Invalid this target for range asList");
            }
        }
    };

    public static final MyLangBuiltinFunction random = new MyLangBuiltinFunction("random") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 0) {
                throw new InterpreterError("Expected 0 arguments, got " + args.size() + " calling function '"+name+"'");
            }
            return interpreter.random.nextDouble();
        }
    };

    public static final MyLangBuiltinFunction clock = new MyLangBuiltinFunction("clock") {
        @Override
        public Object call(MyLangInterpreter interpreter, List<Object> args) {
            if(args.size() != 0) {
                throw new InterpreterError("Expected 0 arguments, got " + args.size() + " calling function '"+name+"'");
            }
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
