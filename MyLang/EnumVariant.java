package MyLang;

import java.util.*;

public record EnumVariant(Token Name, int argCount, Map<String, MyLangCallable> methods, String fileName) implements MyLangCallable {
    public String getName() {
        return Name.lexeme();
    }

    public Object call(MyLangInterpreter interpreter, List<Object> posArgs, Map<String, Object> namedArgs) {
        if(!namedArgs.isEmpty()) {
            throw new InterpreterError("Enum Variant Constructor does not take Named arguments", interpreter.callStack);
        }
        if(posArgs.size() != argCount()) {
            throw new InterpreterError("Invalid number of Arguments to Enum Variant Constructor: Expected "+
                argCount+", got "+posArgs.size(), interpreter.callStack);
        }

        return new EnumVariantObject(posArgs, this);
    }
    @Override
    public String toString() {
        return "<enum constructor '"+Name.lexeme()+"'>";
    }
    public String getFileName() {
        return fileName;
    }
}
