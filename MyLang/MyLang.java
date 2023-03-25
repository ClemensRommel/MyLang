package MyLang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class MyLang {

    private static final MyLangInterpreter interpreter = new MyLangInterpreter();

    // repl data
    private int inputLine = 1;
    
    public void repl() {
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if(line.equals(":quit")) {
                break;
            } else if(line.isBlank()) {
                continue;
            }

            // input ausführen
            // bisher werden nur die Tokens gedruckt
            var tokens = MyLangScanner.tokenize(line, inputLine);
            var expression = MyLangParser.parse(tokens);
            /* for(var token: tokens) {
                System.out.println(token);
            } */
            // System.out.println("Parsed:");
            // System.out.println(expression);
            if(expression.isPresent()) {
                // System.out.println("Interpreted:");
                try {
                    var result = interpreter.interpretAny(expression.get());
                    if (expression.get() instanceof MyLangAST.Expression) {
                        System.out.println(interpreter.stringify(result));
                    }
                } catch (InterpreterError e) {
                    System.out.println("[Error]: "+e.getMessage());
                }
            } else {
                System.out.println("[Error]: could not parse expression");
            }

            inputLine++;
        }
        scanner.close();
    }

    public void runFile(String filename) throws IOException {
        String content = Files.readString(Paths.get(filename));
        // var tokens = MyLangScanner.tokenize(content);
        /* for (var token: tokens) {
            System.out.println(token);
        } */
        var parentDirectory = Paths.get(filename).getParent();
        var program = MyLangParser.parseProgram(content);
        if(program.isPresent()) {
            try{
                interpreter.interpretProgram(program.get(), parentDirectory, true);
            } catch(InterpreterError e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MyLang myLang = new MyLang();
        if(args.length == 1) {
            myLang.runFile(args[0]);
        } else {
            myLang.repl();
        }
    }

}
