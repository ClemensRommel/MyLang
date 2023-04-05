package MyLang;

import java.util.List;
import MyLang.MyLangAST.Type;

public record ParameterInformation(List<Token> names, List<Type> types, Token varargsName, Type varargsType) {

}
