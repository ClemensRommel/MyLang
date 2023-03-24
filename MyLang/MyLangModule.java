package MyLang;

import java.util.Set;
import java.util.HashSet;

public class MyLangModule {
    public Token name = null;
    public final MyLangEnviroment names = new MyLangEnviroment();
    public final Set<String> exports = new HashSet<>();
}