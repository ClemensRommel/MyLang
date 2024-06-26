package MyLang;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.*;

import static MyLang.MyLangAST.*;

public class TypeApplier implements TypeRepVisitor<TypeRep> {
    private Typechecker tc;
    private LinkedList<Map<String, TypeRep>> typeArgs = new LinkedList<>();
    {
        typeArgs.push(new HashMap<>());
    }

    private boolean inferMode = false;

    public TypeApplier(Typechecker t) {
        tc = t;
    }

    public TypeRep apply(TypeFunction t, List<TypeRep> args, boolean inferMode) {
        this.inferMode = inferMode;
        var mapsBefore = typeArgs.size();
        if(t.typeParams().size() != args.size()) {
            if(!inferMode) tc.error("Expected "+
                t.typeParams().size()+
                " Type Parameters but got "+
                args.size()+
                " to type "+tc.p.prettyPrint(t));
            return Typechecker.unknown();
        }
        for(int i  = 0; i < args.size(); i++) {
            typeArgs.peek().put(t.typeParams().get(i).lexeme(), args.get(i));
        }
        var result = t.body().accept(this);
        typeArgs.peek().clear();
        assert typeArgs.size() == mapsBefore;
        return result;
    }
    @Override
    public TypeRep visitTypeIdentifierRep(TypeIdentifierRep i) {
        if (i.env().typeExists(i.name().lexeme())) {
            if (i.env().getTypeByName(i.name().lexeme(), tc) instanceof TypeVar v) {
                return v.accept(this);
            }
        }
        return i;
    }
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
    public TypeRep visitEnumType(EnumType t) {
        var resulting = new EnumType(
            t.name(), 
            t.variants()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> e.getValue().accept(this)
                )),
            t.methods()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> e.getValue().accept(this)
                    )
                ), 
            t.env());
        return resulting;
    }
    @Override
    public TypeRep visitClassType(ClassType c) {
        var resulting = new ClassType(c.name(),
            c.accessors()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    x -> x.getKey(),
                    x -> x.getValue().accept(this)
                )),
            c.readability(),
            c.constructor().accept(this),
            c.env()
            );
        return resulting;
    }
    @Override
    public TypeRep visitGenericType(GenericType g) {
        return new GenericType((TypeFunction) g.t().accept(this));
    }
    @Override
    public TypeRep visitTypeVar(TypeVar t) {
        for(var map: typeArgs) {
            if(map.containsKey(t.name().lexeme())) {
                return map.get(t.name().lexeme());
            } else {
                continue;
            }
        }
        tc.error("Could not find Type Variable "+t.name().lexeme());
        return t;
    }

    @Override
    public TypeRep visitTypeFunction(TypeFunction t) {
        typeArgs.push(t.typeParams().stream()
            .collect(Collectors.toMap(tp -> tp.lexeme(), tp -> new TypeVar(tp, t.env()))));
        var newBody = t.body().accept(this);
        typeArgs.pop();
        return new TypeFunction(t.typeParams(), newBody, t.env());
    }
    @Override
    public TypeRep visitTypeApplication(TypeApplication t) {
        var callee = t.applied().accept(this);
        var args = t.params().stream().map(tp -> tp.accept(this)).toList();
        return new TypeApplication(callee, args);
    }
}
