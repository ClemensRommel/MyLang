package MyLang;


import java.util.*;
import static MyLang.MyLangAST.*;

public record TypeCompiler(Typechecker tc) implements TypeVisitor<TypeRep> {
    public TypeRep compileType(Type t) {
        if(t == null) {
            return null;
        }
        return t.accept(this);
    }
    @Override
    public TypeRep visitNumberType(NumberType n) {
        return Typechecker.numberType;
    }
    @Override
    public TypeRep visitStringType(StringType str) {
        return Typechecker.stringType;
    }
    @Override
    public TypeRep visitBooleanType(BooleanType b) {
        return Typechecker.booleanType;
    }
    @Override
    public TypeRep visitVoidType(VoidType v) {
        return Typechecker.voidType;
    }
    @Override
    public TypeRep visitListOf(ListOf l) {
        return new ListOfRep(compileType(l.elements()));
    }
    @Override
    public TypeRep visitFunctionType(FunctionType f) {
        return new FunctionTypeRep(
                f.parameters().stream().map(this::compileType).toList(),
                compileType(f.varargsType()),
                compileType(f.returnType()),
                tc.env);
    }
    @Override
    public TypeRep visitTypeIdentifier(TypeIdentifier ti) {
        return new TypeIdentifierRep(ti.name(), tc.env);
    }

    @Override
    public TypeRep visitAccess(Access a) {
        var from = compileType(a.accessed());
        return new AccessRep(from, a.name());
    }
}
