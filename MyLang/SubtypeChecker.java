package MyLang;

import static MyLang.MyLangAST.*;

public class SubtypeChecker implements TypeRepVisitor<Boolean> {

    private TypeRep target = null;

    public boolean isSubtypeOf(TypeRep to, TypeRep from) {
        if(to instanceof UnknownType) {
            return true;
        }
        target = to;
        return from.accept(this);
    }

    @Override
    public Boolean visitBuiltin(Builtin b) {
        return b.equals(target);
    }
    @Override
    public Boolean visitListOfRep(ListOfRep l) {
        if(target instanceof ListOfRep l2) {
            return isSubtypeOf(l2.elements(), l.elements());
        } else {
            return false;
        }
    }
    @Override
    public Boolean visitAccessRep(AccessRep a) {
        return a.equals(target);
    }
    @Override
    public Boolean visitModule(MyLangAST.Module m) {
        return m.equals(target);
    }
    @Override
    public Boolean visitClassType(ClassType c) {
        return c.equals(target);
    }
    @Override
    public Boolean visitEnumType(EnumType e) {
        return e.equals(target);
    }
    @Override
    public Boolean visitTypeIdentifierRep(TypeIdentifierRep t) {
        return t.equals(target);
    }
    @Override
    public Boolean visitFunctionTypeRep(FunctionTypeRep f) {
        if(target instanceof FunctionTypeRep f2) { // function parameter requires to many arguments
            if(f2.parameters().size() < f.parameters().size()) {
                return false;
            }
            int parametersTested = 0;
            for(int i = 0; i < f.parameters().size(); i++) {
                parametersTested += 1;
                var inputParameter = f.parameters().get(i);
                var outputParameter = f2.parameters().get(i);
                if(!isSubtypeOf(inputParameter, outputParameter)) {
                    return false;
                }
            }
            if(f2.parameters().size() > parametersTested) { // target type requires more types
                if(f.varargsType() == null) { // function parameter does not have a vararg that could use the remaining types
                    return false;
                }
                if(!f2.parameters()
                        .subList(parametersTested, f2.parameters().size())
                        .stream()
                        .allMatch(t -> isSubtypeOf(f.varargsType(), t))) { // varargs cannot take the remaining arguments 
                            return false;
                        } else {
                            return true;
                        }
            }
            
            // Optional params
            if(f.optionalParameters().size() < f2.optionalParameters().size()) {
                return false;
            }
            boolean optionalsAreOk = true;
            for(int i = 0; i < f2.optionalParameters().size(); i++) {
                optionalsAreOk &= isSubtypeOf(f.optionalParameters().get(i), f2.optionalParameters().get(i));
            }
            if(!optionalsAreOk) {
                return false;
            }

            if(f2.varargsType() != null) {// there is varargs required
                return isSubtypeOf(f.varargsType(), f2.varargsType());
            }

            // Check named params
            boolean namedAreGiven = f.named() // All required params are given
                .entrySet()
                .stream()
                .allMatch((var entry) -> {
                   return (f2.named().containsKey(entry.getKey()) && 
                    isSubtypeOf(entry.getValue(), f2.named().get(entry.getKey())));
                });
            boolean allOptionalNamedAreGiven = f2.optionalNamed().entrySet().stream() // All optional params can be used
                .allMatch(entry -> {
                    return (f.optionalNamed().containsKey(entry.getKey())
                     && isSubtypeOf(f.optionalNamed().get(entry.getKey()), entry.getValue()));
            });
            boolean allNamedAreUsed = f2.named() // all required params can be used
                .entrySet()
                .stream()
                .allMatch(entry -> {
                    return (
                            f.named().containsKey(entry.getKey()) && 
                            isSubtypeOf(f.named().get(entry.getKey()), entry.getValue()))
                        ||  (f.optionalNamed().containsKey(entry.getKey()) &&
                            isSubtypeOf(f.optionalNamed().get(entry.getKey()), entry.getValue()));
            });
            return namedAreGiven && allOptionalNamedAreGiven && allNamedAreUsed && isSubtypeOf(f2.returnType(), f.returnType());

        } else {
            return false;
        }
    }
    @Override
    public Boolean visitUnknownType(UnknownType u) {
        return false;
    }
    @Override
    public Boolean visitNever(Never n) {
        return true;
    }
    @Override
    public Boolean visitTupleRep(TupleRep t) {
        if(target instanceof TupleRep t2) {
            if(t2.elements().size() != t.elements().size()) {
                return false;
            }
            boolean allOk = true;
            for(int i = 0; i < t.elements().size(); i++) {
                allOk &= isSubtypeOf(t2.elements().get(i), t.elements().get(i));
            }
            return allOk;
        }
        return false;
    }
    @Override
    public Boolean visitGenericType(GenericType g) {
        return g.equals(target);
    }
    @Override
    public Boolean visitTypeVar(TypeVar t) {
        return t.equals(target);
    }
    @Override
    public Boolean visitTypeFunction(TypeFunction t) {
        return false;
    }
}
