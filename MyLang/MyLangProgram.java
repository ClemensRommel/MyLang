package MyLang;

import java.util.List;

import MyLang.MyLangAST.Declaration;

public record MyLangProgram(List<Declaration> stmts) {

}