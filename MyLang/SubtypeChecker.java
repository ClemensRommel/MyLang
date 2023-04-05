package MyLang;

import static MyLang.MyLangAST.*;

public class SubtypeChecker implements TypeRepVisitor<Boolean> {

    private TypeRep superType = null;

    public boolean isSubtypeOf(TypeRep to, TypeRep from) {
        if(to instanceof UnknownType) {
            return true;
        }
        superType = to;
        return from.accept(this);
    }

    @Override
    public Boolean visitBuiltin(Builtin b) {
        return b.equals(superType);
    }
    @Override
    public Boolean visitListOfRep(ListOfRep l) {
        if(superType instanceof ListOfRep l2) {
            return isSubtypeOf(l2.elements(), l.elements());
        } else {
            return false;
        }
    }
    @Override
    public Boolean visitAccessRep(AccessRep a) {
        return a.equals(superType);
    }
    @Override
    public Boolean visitModule(MyLangAST.Module m) {
        return m.equals(superType);
    }
    @Override
    public Boolean visitClassType(ClassType c) {
        return c.equals(superType);
    }
    @Override
    public Boolean visitTypeIdentifierRep(TypeIdentifierRep t) {
        return t.equals(superType);
    }
    @Override
    public Boolean visitFunctionTypeRep(FunctionTypeRep f) {
        if(superType instanceof FunctionTypeRep f2) { // function parameter requires to many arguments
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
            if(f2.varargsType() != null) {// there is varargs required
                return isSubtypeOf(f.varargsType(), f2.varargsType());
            }
            return isSubtypeOf(f2.returnType(), f.returnType());

        } else {
            return false;
        }
    }
    @Override
    public Boolean visitUnknownType(UnknownType u) {
        return false;
    }
}
