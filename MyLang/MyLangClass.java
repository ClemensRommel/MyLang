package MyLang;

import java.util.Map;
import java.util.List;
import MyLang.MyLangAST.*;

public record MyLangClass(
        String name, 
        Map<String, MyLangCallable> methods, 
        List<VariableDeclaration> fields, 
        MyLangCallable constructor, 
        MyLangEnviroment env,
        String fileName) implements MyLangCallable {

    public String getName() {
        return name;
    }
    public String getFileName() {
        return fileName;
    }

    public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> namedArgs) {
        MyLangEnviroment previousEnv = interpreter.env;
        interpreter.env = env.openScope();

        MyLangObject instance = new MyLangObject();
        instance.klass = this;
        for(var decl : fields) {
            instance.readability.put(
                ((VariableBinding) decl.pat()).name().lexeme(), 
                decl.isReassignable());

            instance.fields.put(
                ((VariableBinding) decl.pat()).name().lexeme(), 
                interpreter.interpretExpression(decl.initializer()));
        }
        if(constructor != null) {
            boolean prevInConstructor = interpreter.inConstructor;
            interpreter.inConstructor = true;
            constructor.bind(instance).call(interpreter, args, namedArgs);
            interpreter.inConstructor = prevInConstructor;
        } else {
            if(args.size() != 0 && !namedArgs.isEmpty()) {
                throw new InterpreterError("Invalid number of arguments to implicit constructor of class "+name+": "+args.size());
            }
        }

        interpreter.env = previousEnv;

        return instance;
    }

    @Override
    public String toString() {
        return "<class '"+name+"'>";
    }
}
