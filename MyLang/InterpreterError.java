package MyLang;

import java.util.LinkedList;

public class InterpreterError extends RuntimeException {
    LinkedList<MyLangStacktraceElement> callStack;
    public InterpreterError(String message, LinkedList<MyLangStacktraceElement> callStack) {
        super(message); this.callStack = callStack;
    }

    @Override
    public void printStackTrace() {
        System.out.println(getMessage()+ ": ");
        for(MyLangStacktraceElement s : callStack) {
            System.out.println("\t"+s.name());
        }
    }
}
