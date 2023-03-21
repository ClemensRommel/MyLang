package MyLang;

import java.util.HashMap;
import java.util.Map;

public class MyLangObject {
    MyLangClass klass;
    Map<String, Object> fields = new HashMap<>();
    Map<String, Boolean> readability = new HashMap<>();

    public Object getField(String name) {
        if(fields.containsKey(name)) {
            return fields.get(name);
        } else if(klass.methods().containsKey(name)) {
            return klass.methods().get(name).bind(this);
        } else {
            throw new InterpreterError("Object of class "+klass.name()+" has no field '"+name+"'");
        }
    }

    public void setField(String name, Object value) {
        if(klass.methods().containsKey(name)) {
            throw new InterpreterError("Cannot reassign methods of objects");
        } else if(fields.containsKey(name)) {
            if(readability.get(name)) {
                fields.put(name, value);
            } else {
                throw new InterpreterError("Error: cannot write to field that is not reassignable: "+name +" (class is "+ klass.name()+")");
            }
        } else {
            throw new InterpreterError("Object of class "+klass.name()+" has no field '"+name+"'");
        }
    }

    @Override
    public String toString() {
        return "<object of class '"+klass.name()+"'>";
    }
}
