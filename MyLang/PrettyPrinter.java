package MyLang;

import static MyLang.MyLangAST.*;

import java.util.*;

public class PrettyPrinter 
implements TypeRepVisitor<Void> {
    private StringBuilder builder = null;

    public String prettyPrint(TypeRep t) {
        builder = new StringBuilder();
        t.accept(this);
        var result = builder.toString();
        builder = null;
        return result;
    }

    @Override
    public Void visitBuiltin(Builtin b) {
        builder.append(switch(b.type()) {
            case NUMBER -> "Number";
            case BOOLEAN -> "Boolean";
            case STRING -> "String";
            case VOID -> "Void";
        });

        return null;
    }

    @Override
    public Void visitListOfRep(ListOfRep r) {
        if(r.elements() instanceof FunctionTypeRep f) {
            builder.append("(");
            f.accept(this);
            builder.append(")");
        } else {
            r.elements().accept(this);
        }
        builder.append("[]");
        return null;
    }
    @Override
    public Void visitUnknownType(UnknownType u) {
        builder.append("<unknown>");
        return null;
    }
    @Override
    public Void visitAccessRep(AccessRep a) {
        if(a.accessed() instanceof FunctionTypeRep f) {
            builder.append("(");
            f.accept(this);
            builder.append(")");
        } else {
            a.accessed().accept(this);
        }
        builder.append(".");
        builder.append(a.name().lexeme());
        return null;
    }

    @Override
    public Void visitTypeIdentifierRep(TypeIdentifierRep ti) {
        builder.append(ti.name().lexeme());
        return null;
    }
    @Override
    public Void visitClassType(ClassType c) {
        builder.append(c.name().lexeme());
        return null;
    }
    @Override
    public Void visitModule(MyLangAST.Module m) {
        builder.append(m.name());
        return null;
    }
    @Override
    public Void visitFunctionTypeRep(FunctionTypeRep f) {
        builder.append("Fun(");
        if(f.parameters().isEmpty() && f.varargsType() == null) {
            builder.append(")");
        } else if(f.parameters().isEmpty() && f.varargsType() != null) {
            if(f.varargsType() instanceof FunctionTypeRep f2) {
                builder.append("(");
                f2.accept(this);
                builder.append(")");
            } else {
                f.varargsType().accept(this);
            }
            builder.append("..)");
        } else if(!f.parameters().isEmpty() && f.varargsType() != null) {
            for(var typeRep : f.parameters()) {
                typeRep.accept(this);
                builder.append(",");
            }
            if(f.varargsType() instanceof FunctionTypeRep f2) {
                builder.append("(");
                f2.accept(this);
                builder.append(")");
            } else {
                f.varargsType().accept(this);
            }
            builder.append("..)");
        } else if(!f.parameters().isEmpty() && f.varargsType() == null) {
            for(var typeRep: f.parameters().subList(0, f.parameters().size()-1)) {
                typeRep.accept(this);
                builder.append(",");
            }
            f.parameters().get(f.parameters().size()-1).accept(this);
            builder.append(")");
        }
        builder.append(": ");
        f.returnType().accept(this);
        return null;
    }
}
