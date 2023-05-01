package MyLang;


import java.util.*;
import java.util.stream.Collectors;
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
        if(f.varargsType() != null & !f.optionalParameters().isEmpty()) {
            tc.error("Cannot have both varargs and optional Parameters");
        }
        return new FunctionTypeRep(
                f.parameters().stream().map(this::compileType).toList(),
                f.optionalParameters().stream().map(this::compileType).toList(),
                namedTypesIn(f.named()),
                namedTypesIn(f.optionalNamed()),
                compileType(f.varargsType()),
                compileType(f.returnType()),
                tc.env);
    }
    Map<String, TypeRep> namedTypesIn(Map<String, Type> named) {
        return named.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey, 
                value -> compileType(value.getValue())));
    }
    Map<String, TypeRep> optionalNamedTypesIn(Map<String, OptionalParam> on) {
        return on.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                value -> compileType(value.getValue().type())
        ));
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
    @Override
    public TypeRep visitTuple(Tuple t) {
        return new TupleRep(t.types().stream()
            .map(this::compileType)
            .toList());
    }
}
