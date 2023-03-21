package MyLang;

import java.util.Map;
import java.util.HashMap;

public class MyLangEnviroment {
    private MyLangEnviroment outer = null;
    private Map<String, Object> variables = new HashMap<>();
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
        if(!variables.containsKey(value)) {
            variables.put(name, value);
            readability.put(name, readable);
        } else {
            throw new InterpreterError("Variable '"+name+"' already declared");
        }
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
}
