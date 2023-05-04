package MyLang;

import java.io.IOException;

public class MyLang {
    public void runFile(String filename) throws IOException {
        MyLangRunner runner = new MyLangRunner(filename);
        runner.run();
    }

    public static void main(String[] args) throws IOException {
        MyLang myLang = new MyLang();
        if(args.length == 1) {
            myLang.runFile(args[0]);
        } else {
            System.err.println("""
                Verwendung:
                java MyLang/MyLang <eingabedatei>
                """);
        }
    }

}
