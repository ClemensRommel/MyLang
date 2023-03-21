package MyLang;

import java.util.List;

import MyLang.MyLangAST.DeclarationOrStatement;

public record MyLangProgram(List<DeclarationOrStatement> stmts) {

}