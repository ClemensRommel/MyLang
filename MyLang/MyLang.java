package MyLang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MyLang {

    ArrayList<String> program_args;
    public void runFile(String filename) throws IOException {
        MyLangRunner runner = new MyLangRunner(filename, program_args);
        runner.run();
    }

    public static void main(String[] args) throws IOException {
        MyLang myLang = new MyLang();
        if(args.length >= 1) {
            myLang.program_args = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
            myLang.runFile(args[0]);
        } else {
            System.err.println("""
                Verwendung:
                java MyLang/MyLang <eingabedatei>
                """);
        }
    }

}
