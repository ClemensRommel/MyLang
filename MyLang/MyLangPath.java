package MyLang;

import java.util.List;

public record MyLangPath(List<Token> names) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(names.get(0).lexeme());
        for(var name: names.subList(1, names.size())) {
            sb.append("/");
            sb.append(name.lexeme());
       }
        return sb.toString();
    } 
}
