package MyLang;

import java.util.LinkedList;

public class InterpreterError extends RuntimeException {
    LinkedList<String> callStack;
    public InterpreterError(String message, LinkedList<String> callStack) {
        super(message); this.callStack = callStack;
    }

    @Override
    public void printStackTrace() {
        System.out.println(getMessage()+ ": ");
        for(String s : callStack) {
            System.out.println("\t"+s);
        }
    }
}
