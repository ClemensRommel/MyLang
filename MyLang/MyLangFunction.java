package MyLang;

import java.util.List;
import java.util.Map;
import MyLang.MyLangAST.Expression;

public record MyLangFunction(
        String name, 
        List<Token> parameters, 
        Token varargsName, 
        List<OptionalParam> optionals,
        Map<String, OptionalParam> optionalNamed,
        MyLangEnviroment env, 
        Expression body) implements MyLangCallable {

    public String getName() {
        return name;
    }

    @Override
    public Object call(
        MyLangInterpreter interpreter, 
        List<Object> args, 
        Map<String, Object> namedArgs) {

        MyLangEnviroment previousEnv = interpreter.env;
        interpreter.env = env.openScope();

        if(!checkSize(args)) {
            throw new InterpreterError(
                    "Wrong number of arguments: expected "+parameters.size()+
                    ", got "+args.size()+" ("+args+")");
        }
        for(int i = 0; i < parameters.size(); i++) {
            interpreter.env.declareVariable(parameters.get(i).lexeme(), args.remove(0), false);
        }
        var remaining  = args;
        for(var param: optionals) {
            if(remaining.isEmpty()) {
                interpreter.env.declareVariable(param.name(), interpreter.interpretExpression(param.defaultValue()), false);
            } else {
                interpreter.env.declareVariable(param.name(), remaining.remove(0), false);
            }
        }
        if(varargsName != null) {
            var varargs = remaining;
            interpreter.env.declareVariable(varargsName.lexeme(), varargs, false);
        }
        namedArgs.forEach((var name,var value) -> {
            interpreter.env.declareVariable(name, value, false);
        });
        optionalNamed.forEach((var name, var value) -> {
            if(!interpreter.env.localVariableDeclared(name)) {
                interpreter.env.declareVariable(name, interpreter.interpretExpression(value.defaultValue()), false);
            }
        });
        Object result;
        try {
            result = interpreter.interpretExpression(body);
        } catch(ReturnException r) {
            result = r.returnValue;
        }
        interpreter.env = previousEnv;

        return result;
    }

    private boolean checkSize(List<Object> args) {
        return (varargsName != null && args.size() >= parameters.size()) 
            || (args.size() >= parameters.size() && args.size() <= parameters.size() + optionals.size());
    }

    @Override
    public String toString() {
        return "<function '"+name+"'>";
    }
}
