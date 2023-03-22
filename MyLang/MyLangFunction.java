package MyLang;

import java.util.List;
import MyLang.MyLangAST.Expression;

public record MyLangFunction(String name, List<Token> parameters, Token varargsName, MyLangEnviroment env, Expression body) implements MyLangCallable {
    public String getName() {
        return name;
    }

    @Override
    public Object call(MyLangInterpreter interpreter, List<Object> args) {
        MyLangEnviroment previousEnv = interpreter.env;
        interpreter.env = env.openScope();

        if(!checkSize(args)) {
            throw new InterpreterError("Wrong number of arguments: expected "+parameters.size()+", got "+args.size()+" ("+args+")");
        }
        for(int i = 0; i < parameters.size(); i++) {
            interpreter.env.declareVariable(parameters.get(i).lexeme(), args.get(i), false);
        }
        if(varargsName != null) {
            var varargs = args.subList(parameters.size(), args.size());
            interpreter.env.declareVariable(varargsName.lexeme(), varargs, false);
        }

        var result = interpreter.interpretExpression(body);

        interpreter.env = previousEnv;

        return result;
    }

    private boolean checkSize(List<Object> args) {
        return (varargsName != null && args.size() >= parameters.size()) 
            || args.size() == parameters.size();
    }

    @Override
    public String toString() {
        return "<function '"+name+"'>";
    }
}
