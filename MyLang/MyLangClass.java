package MyLang;

import java.util.Map;
import java.util.List;
import MyLang.MyLangAST.*;

public record MyLangClass(String name, Map<String, MyLangCallable> methods, List<VariableDeclaration> fields, MyLangCallable constructor, MyLangEnviroment env) implements MyLangCallable {
    public String getName() {
        return name;
    }
    public Object call(MyLangInterpreter interpreter, List<Object> args) {
        MyLangEnviroment previousEnv = interpreter.env;
        interpreter.env = env.openScope();

        MyLangObject instance = new MyLangObject();
        instance.klass = this;
        for(var decl : fields) {
            instance.readability.put(decl.Name().lexeme(), decl.isReassignable());
            instance.fields.put(decl.Name().lexeme(), interpreter.interpretExpression(decl.initializer()));
        }
        if(constructor != null) {
            constructor.bind(instance).call(interpreter, args);
        } else {
            if(args.size() != 0) {
                throw new InterpreterError("Invalid number of arguments to implicit constructor of class "+name+": "+args.size());
            }
        }

        interpreter.env = previousEnv;

        return instance;
    }
}
