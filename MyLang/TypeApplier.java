package MyLang;

import java.util.List;
import java.util.Map;
import java.util.stream.*;

import static MyLang.MyLangAST.*;

public class TypeApplier implements TypeRepVisitor<TypeRep> {
    private Typechecker tc;

    public TypeApplier(Typechecker t) {
        tc = t;
    }

    public TypeRep apply(TypeRep t, List<TypeRep> args, boolean inferMode) {
        if(tc.env.normalize(t, tc) instanceof GenericType g) {
            if(g.typeParams().size() != args.size()) {
                if(!inferMode) tc.error("Expected "+
                    g.typeParams().size()+
                    " Type Parameters but got "+
                    args.size()+
                    " to type "+tc.p.prettyPrint(t));
                return Typechecker.unknown();
            }
            tc.openScope();
            for(int i  = 0; i < args.size(); i++) {
                tc.env.addType(g.typeParams().get(i).lexeme(), args.get(i));
            }
            var result = g.type().accept(this);
            tc.closeScope();
            return result;
        } else {
            StringBuilder arguments = new StringBuilder();
            arguments.append("[");
            boolean needComma = false;
            for(var param : args) {
                if(needComma) arguments.append(", ");
                arguments.append(tc.p.prettyPrint(param));
                needComma = true;
            }
            arguments.append("]");
            if(!inferMode) tc.error("Tried to apply non-generic Type "+tc.p.prettyPrint(t) + "' to type Arguments "+arguments.toString());
            return Typechecker.unknown();
        }
    }
    @Override
    public TypeRep visitTypeIdentifierRep(TypeIdentifierRep i) {return i;}
    @Override
    public TypeRep visitFunctionTypeRep(FunctionTypeRep f) {
        return new FunctionTypeRep(
            f.parameters().stream().map(x -> x.accept(this)).toList(),
            f.optionalParameters().stream().map(x -> x.accept(this)).toList(),
            f.named().entrySet().stream().collect(Collectors.toMap(
                x -> x.getKey(),
                x -> x.getValue().accept(this))),
            f.optionalNamed().entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> entry.getValue().accept(this)
            )),
            f.varargsType() != null ? f.varargsType().accept(this) : null,
            f.returnType().accept(this),
            f.env()
        );
    }
    @Override
    public TypeRep visitListOfRep(ListOfRep l) {return new ListOfRep(l.elements().accept(this));}
    @Override
    public TypeRep visitTupleRep(TupleRep t) {return new TupleRep(t.elements().stream().map(x -> x.accept(this)).toList());}
    @Override
    public TypeRep visitAccessRep(AccessRep a) {return new AccessRep(
        a.accessed().accept(this), a.name());}
    @Override
    public TypeRep visitNever(Never n) {return n;}
    public TypeRep visitBuiltin(Builtin b) {return b;}
    @Override
    public TypeRep visitUnknownType(UnknownType u) {return u;}
    @Override
    public TypeRep visitModule(MyLangAST.Module m) {return m;}
    @Override
    public TypeRep visitEnumType(EnumType t) {return t;}
    @Override
    public TypeRep visitClassType(ClassType c) {return c;}
    @Override
    public TypeRep visitGenericType(GenericType g) {
        throw new RuntimeException("Reached a nested Generic type while applying type "+tc.p.prettyPrint(g));
    }
    @Override
    public TypeRep visitTypeVar(TypeVar t) {
        if(tc.env.typeExists(t.name().lexeme())) {
            return tc.env.getTypeByName(t.name().lexeme());
        } else {
            tc.error("Undefined type Variable '"+t.name().lexeme());
            return t;
        }
    }

}
