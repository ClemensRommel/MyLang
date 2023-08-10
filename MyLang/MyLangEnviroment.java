package MyLang;

import java.util.Map;
import java.util.HashMap;

public class MyLangEnviroment {
    private MyLangEnviroment outer = null;
    Map<String, Object> variables = new HashMap<>();
    private Map<String, Boolean> readability = new HashMap<>();

    public MyLangEnviroment(MyLangEnviroment outer) {
        this.outer = outer;
    }
    public MyLangEnviroment() {
        
    }

    public MyLangEnviroment closeScope() {
        return outer;
    }

    public MyLangEnviroment openScope() {
        return new MyLangEnviroment(this);
    }

    public void declareVariable(String name, Object value, boolean readable) {
        variables.put(name, value);
        readability.put(name, readable);
    }

    public boolean localVariableDeclared(String name) {
        return variables.containsKey(name) || (outer != null && outer.localVariableDeclared(name));
    }
    
    public Object getVariable(String name) {
        if(variables.containsKey(name)) {
            return variables.get(name);
        } else if(outer != null) {
            return outer.getVariable(name);
        } else {
            throw new InterpreterError("Variable '"+name+"' not declared");
        }
    }

    public void setVariable(String name, Object value) {
        if(variables.containsKey(name) && readability.get(name)) {
            variables.put(name, value);
        } else if(outer != null) {
            outer.setVariable(name, value);
        } else {
            throw new InterpreterError("Variable '"+name+"' not declared");
        }
    }

    public boolean isInClass() {
        return variables.containsKey("this") || (outer != null && outer.isInClass());
    }
    public void declareModule(MyLangPath path, MyLangModule module) {
        MyLangEnviroment currentEnclosing = this;
        for(int i = 0; i < path.names().size() - 1; i++) {
            Token name = path.names().get(i);
            if(currentEnclosing.variables.containsKey(name.lexeme())) {
                var nextModule = (MyLangModule) currentEnclosing.getVariable(name.lexeme());
                currentEnclosing = nextModule.names;
            } else {
                var newModule = new MyLangModule();
                newModule.name = new MyLangPath(path.names().subList(0, i));
                newModule.exports.add(path.names().get(i+1).lexeme());
                currentEnclosing.declareVariable(name.lexeme(), newModule, false);
                currentEnclosing = newModule.names;
            }
        }
        currentEnclosing.declareVariable(path.names().get(path.names().size()-1).lexeme(), module, false);
    }
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(variables);
        if(outer != null) {
            b.append(" in:\n");
            b.append(outer.toString());
        }
        return b.toString();
    }
}
