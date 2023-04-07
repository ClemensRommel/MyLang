package MyLang;

import java.util.List;
import java.util.Map;
import MyLang.MyLangAST.Type;

public record ParameterInformation(
    List<Token> names, 
    List<Type> types, 
    List<OptionalParam> optionals,
    Map<String, Type> named, 
    Map<String, OptionalParam> optionalNamed,
    Token varargsName, 
    Type varargsType) {

}
