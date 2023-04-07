package MyLang;

import static MyLang.MyLangAST.*;

public record OptionalParam(String name, Type type, Expression defaultValue) {

}
