package MyLang;

import java.util.List;
import MyLang.MyLangAST.Expression;

public record MyLangFunction(String name, List<Token> parameters, MyLangEnviroment env, Expression body) implements MyLangCallable {
    public String getName() {
        return name;
    }

    @Override
    public Object call(MyLangInterpreter interpreter, List<Object> args) {
        MyLangEnviroment previousEnv = interpreter.env;
        interpreter.env = env.openScope();

        if(args.size() != parameters.size()) {
            throw new InterpreterError("Wrong number of arguments");
        }
        for(int i = 0; i < parameters.size(); i++) {
            interpreter.env.declareVariable(parameters.get(i).lexeme(), args.get(i), false);
        }

        var result = interpreter.interpretExpression(body);

        interpreter.env = previousEnv;

        return result;
    }

    
}
