package MyLang;

import java.util.List;
import static MyLang.MyLangAST.*;

public record MyLangFile(List<Declaration> declarations, List<Import> imports, String fileName) {
    
}
