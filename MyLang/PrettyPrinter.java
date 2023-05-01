package MyLang;

import static MyLang.MyLangAST.*;

public class PrettyPrinter implements 
    TypeRepVisitor<Void>, TypeVisitor<Void>, 
    ExpressionVisitor<Void>, ParameterVisitor<Void>, PatternVisitor<Void>,
    DeclarationVisitor<Void>, ConstructorVisitor<Void>, StatementVisitor<Void>, SetterVisitor<Void> {
    private StringBuilder builder = null;

    public String prettyPrint(TypeRep t) {
        builder = new StringBuilder();
        t.accept(this);
        var result = builder.toString();
        builder = null;
        return result;
    }
    public String prettyPrint(Setter s) {
        builder = new StringBuilder();
        s.accept(this);
        var result = builder.toString();
        builder = null;
        return result;
    }
    public String prettyPrint(Expression e) {
        builder = new StringBuilder();
        e.accept(this);
        var result = builder.toString();
        builder = null;
        return result;
    }

    public String prettyPrint(DeclarationOrStatement e) {
        builder = new StringBuilder();
        visitStmt(e);
        var result = builder.toString();
        builder = null;
        return result;
    } 

    public String prettyPrint(Parameter p) {
        builder = new StringBuilder();
        p.accept(this);
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
    public Void visitVoidType(VoidType t) {
        builder.append("Void");
        return null;
    }
    @Override
    public Void visitNumberType(NumberType n) {
        builder.append("Number");
        return null;
    }
    @Override
    public Void visitBooleanType(BooleanType v) {
        builder.append("Bool");
        return null;
    }
    @Override
    public Void visitStringType(StringType s) {
        builder.append("String");
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
    public Void visitListOf(ListOf l) {
        var elementType = l.elements();
        if (elementType instanceof FunctionType f) {
            builder.append("(");
            f.accept(this);
            builder.append(")");
        } else {
            elementType.accept(this);
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
    public Void visitAccess(Access a) {
        if(a.accessed() instanceof FunctionType f) {
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
    public Void visitTypeIdentifier(TypeIdentifier t) {
        builder.append(t.name().lexeme());
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

    @Override
    public Void visitFunctionType(FunctionType f) {
        builder.append("Fun")
               .append("(");
        boolean needComma = false;
        for(var param : f.parameters()) {
            if(needComma) {
                builder.append(", ");
            }
            param.accept(this);
            needComma = true;
        }
        if(f.varargsType() != null) {
            if(needComma) {
                builder.append(", ");
            }
            f.varargsType().accept(this);
            builder.append("..");
            needComma = true;
        }
        if(!f.optionalParameters().isEmpty()) {
            if(needComma) builder.append(", ");
            builder.append("[");
            needComma = false;
            for(var param: f.optionalParameters()) {
                if(needComma) builder.append(", ");
                param.accept(this);
                needComma = true;
            }
            builder.append("]");
            needComma = true;
        }
        if(!f.named().isEmpty() || !f.optionalNamed().isEmpty()) {
            if(needComma) builder.append(", ");
            builder.append("{");
            needComma = false;
            for(var param : f.named().entrySet()) {
                if(needComma) builder.append(", ");
                builder.append(param.getKey());
                builder.append(": ");
                param.getValue().accept(this);
                needComma = true;
            }
            for(var oparam : f.optionalNamed().entrySet()) {
                if(needComma) builder.append(", ");
                builder.append(oparam.getKey());
                builder.append("?: ");
                oparam.getValue().accept(this);
                needComma = true;
            }
            builder.append("}");
            needComma = true;
        }
        builder.append("): ");
        f.returnType().accept(this);

        return null;
    }

    @Override
    public Void visitNumericLiteral(NumericLiteral n) {
        builder.append(n.value());
        
        return null;
    }
    @Override
    public Void visitStringLiteral(StringLiteral s) {
        builder.append("\"");
        builder.append(s.value().replace("\"", "\\\""));
        builder.append("\"");

        return null;
    }
    @Override
    public Void visitBooleanLiteral(BooleanLiteral b) {
        builder.append(b.value() ? "true" : false);

        return null;
    }

    @Override
    public Void visitNullLiteral(NullLiteral n) {
        builder.append("null");

        return null;
    }

    @Override
    public Void visitIdentifier(Identifier i) {
        builder.append(i.value().lexeme());

        return null;
    }

    @Override
    public Void visitBinaryOperation(BinaryOperation b) {
        if(b.operator().type() == TokenType.STAR && !"*".equals(b.operator().lexeme())) {
            b.left().accept(this);
            builder.append(" * (");
            b.right().accept(this);
            builder.append(")");
        } else {
            builder.append("(");
            b.left().accept(this);
            builder.append(") ");
            builder.append(b.operator().lexeme());
            builder.append(" (");
            b.right().accept(this);
            builder.append(")");
        }

        return null;
    }
    @Override
    public Void visitUnaryOperation(UnaryOperation o) {
        builder.append(o.operator().lexeme());
        if(o.operator().lexeme().equals("not")) builder.append(" ");
        o.operand().accept(this);

        return null;
    }

    @Override
    public Void visitFunctionCall(FunctionCall f) {
        f.callee().accept(this);
        builder.append("(");
        for(int i = 0; i < f.arguments().size(); i++) {
            f.arguments().get(i).accept(this);
            if(i != f.arguments().size() - 1) {
                builder.append(",");
            }
        }
        builder.append(")");

        return null;
    }
    @Override
    public Void visitFunctionExpression(FunctionExpression f) {
        builder.append("fun");
        builder.append("(");
        prettyPrintParameters(f.parameters());
        builder.append("): ");
        f.retType().accept(this);
        builder.append(" := ");
        f.body().accept(this);

        return null;
    }

    private void prettyPrintParameters(ParameterInformation p) {
        boolean addComma = false;
        for(int i = 0; i < p.names().size(); i++) { // Normal Parameters
            builder.append(p.names().get(i).lexeme());
            builder.append(":");
            p.types().get(i).accept(this);
            if(i != p.names().size() - 1) {
                builder.append(",");
            }
            addComma = true;
        }
        if(!p.optionals().isEmpty()) { // Optional Parameters
            if(addComma) { // There where normal parameters before
                builder.append(",");
            }
            builder.append("[");
            for(int i = 0; i < p.optionals().size(); i++) {
                builder.append(p.optionals().get(i).name());
                builder.append(":");
                p.optionals().get(i).type().accept(this);
                if(i != p.optionals().size() - 1) {
                    builder.append(",");
                }   
            }
            builder.append("]");
            addComma = true;
        }
        if(!p.named().isEmpty() || !p.optionalNamed().isEmpty()) { // Named Parameters
            if(addComma) {
                builder.append(",");
            }
            boolean alreadyAddedEntry = false;
            builder.append("{");
            for(var entry: p.named().entrySet()) {
                if(alreadyAddedEntry) {
                    builder.append(",");
                }
                builder.append(entry.getKey());
                builder.append(": ");
                builder.append(entry.getValue().accept(this));
                alreadyAddedEntry = true;
            }
            for(var entry: p.optionalNamed().entrySet()) {
                if(alreadyAddedEntry) {
                    builder.append(",");
                }
                builder.append(entry.getKey());
                builder.append("?: ");
                builder.append(entry.getValue().type().accept(this));
            }
            builder.append("}");
        }
    }

    @Override
    public Void visitIfExpression(IfExpression i) {
        builder.append("if ");
        i.condition().accept(this);
        builder.append(" then ");
        i.thenBranch().accept(this);
        builder.append(" else ");
        i.elseBranch().accept(this);

        return null;
    }
    @Override
    public Void visitIfValExpression(IfValExpression i) {
        builder.append("if val ");
        i.pat().accept(this);
        builder.append(" := ");
        i.matched().accept(this);
        builder.append(" then ");
        i.thenBranch().accept(this);
        builder.append(" else ");
        i.elseBranch().accept(this);
        
        return null;
    }

    @Override
    public Void visitListExpression(ListExpression l) {
        builder.append("[");
        boolean alreadyAddedElement = false;
        for(var elem: l.elements()) {
            if(alreadyAddedElement) {
                builder.append(", ");
            }
            elem.accept(this);
        }
        builder.append("]");

        return null;
    }

    @Override
    public Void visitIndexExpression(IndexExpression i) {
        builder.append("(");
        i.list().accept(this);
        builder.append(")");
        builder.append("[");
        i.index().accept(this);
        builder.append("]");

        return null;
    }
    @Override
    public Void visitPropertyExpression(PropertyExpression p) {
        builder.append("(");
        p.object().accept(this);
        builder.append(").");
        builder.append(p.name());
        
        return null;
    }
    @Override
    public Void visitBlockExpression(BlockExpression b) {
        builder.append("do ");
        for(var stmt: b.statements()) {
            visitStmt(stmt);
        }
        b.returnValue().accept(this);
        builder.append("end");

        return null;
    }
    private void visitStmt(DeclarationOrStatement d) {
        if(d instanceof Declaration d2) {
            d2.accept(this);
        } else if(d instanceof Statement s) {
            s.accept(this);
        } else {
            throw new UnsupportedOperationException("Cannot visit unsupported statement");
        }
    }
    @Override
    public Void visitWhileYieldExpression(WhileYieldExpression w) {
        builder.append("while ");
        w.condition().accept(this);
        builder.append(" yield ");
        w.body().accept(this);

        return null;
    }
    @Override
    public Void visitWhileValYieldExpression(WhileValYieldExpression w) {
        builder.append("while val ");
        w.pattern().accept(this);
        builder.append(" := ");
        w.matched().accept(this);
        builder.append(" yield ");
        w.body().accept(this);

        return null;
    }

    @Override
    public Void visitWhileDoExpression(WhileDoExpression w) {
        builder.append("while ");
        w.condition().accept(this);
        w.body().accept(this);

        return null;
    }
    @Override
    public Void visitWhileValDoExpression(WhileValDoExpression w) {
        builder.append("while val ");
        w.pattern().accept(this);
        builder.append(" := ");
        w.matched().accept(this);
        w.body().accept(this);

        return null;
    }

    @Override
    public Void visitForYieldExpression(ForYieldExpression f) {
        builder.append("for ");
        f.pat().accept(this);
        builder.append(" in ");
        f.collection().accept(this);
        if(f.guard() != null) {
            builder.append(" if ");
            f.guard().accept(this);
        }
        builder.append(" yield ");
        f.body().accept(this);

        return null;
    }

    @Override
    public Void visitForDoExpression(ForDoExpression f) {
        builder.append("for ");
        f.pat().accept(this);
        builder.append(" in ");
        f.collection().accept(this);
        if(f.guard() != null) {
            builder.append(" if ");
            f.guard().accept(this);
        }
        builder.append(" do ");
        f.body().accept(this);

        return null;
    }

    @Override
    public Void visitRangeExpression(RangeExpression r) {
        builder.append("[");
        r.start().accept(this);
        builder.append("..");
        r.end().accept(this);
        builder.append(" : ");
        r.step().accept(this);
        builder.append("]");

        return null;
    }
    @Override
    public Void visitThisExpression(ThisExpression t) {
        builder.append("this");

        return null;
    }

    @Override
    public Void visitExpressionParameter(ExpressionParameter e) {
        return e.expr().accept(this);
    }
    @Override
    public Void visitSpreadParameter(SpreadParameter s) {
        s.collection().accept(this);
        builder.append("..");
        return null;
    }
    @Override
    public Void visitConditionalParameter(ConditionalParameter c) {
        c.body().accept(this);
        builder.append(" if ");
        c.guard().accept(this);

        return null;
    }
    @Override
    public Void visitNamedParameter(NamedParameter n) {
        builder.append(n.name().lexeme());
        builder.append(": ");
        n.parameter().accept(this);

        return null;
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclaration v) {
        builder.append(v.isReassignable() ? "var " : "val ");
        v.pat().accept(this);
        builder.append(" := ");
        v.initializer().accept(this);
        builder.append(";");

        return null;
    }
    @Override
    public Void visitValElseDeclaration(ValElseDeclaration v) {
        builder.append("val ");
        v.pat().accept(this);
        builder.append(" := ");
        v.initializer().accept(this);
        builder.append(" else ");
        v.elseBranch().accept(this);
        builder.append(";");

        return null;
    }
    @Override
    public Void visitFunctionDeclaration(FunctionDeclaration f) {
        builder.append("fun ");
        builder.append(f.Name().lexeme());
        builder.append("(");
        prettyPrintParameters(f.parameters());
        builder.append("): ");
        f.retType().accept(this);
        if(!(f.body() instanceof BlockExpression)) {
            builder.append(" := ");
        }
        f.body().accept(this);
        builder.append(";");

        return null;
    }
    @Override
    public Void visitModuleDeclaration(ModuleDeclaration m) {
        builder.append("module ");
        boolean addDot = false;
        for(var name : m.Name().names()) {
            if(addDot) builder.append(".");
            builder.append(name.lexeme());
            addDot = true;
        } 
        builder.append(";");

        return null;
    }
    @Override
    public Void visitEmptyDeclaration(EmptyDeclaration e) {
        builder.append(";");
        return null;
    }
    @Override
    public Void visitTypeDefDeclaration(TypeDefDeclaration t) {
        builder.append("type ");
        builder.append(t.Name());
        builder.append(" := ");
        t.definition().accept(this);
        builder.append(";");

        return null;
    }
    @Override
    public Void visitClassDeclaration(ClassDeclaration c) {
        builder.append("class ");
        builder.append(c.Name().lexeme());
        builder.append(" where ");
        for(var decl: c.fieldsAndMethods()) {
            decl.accept(this);
        }
        c.constructor().accept(this);
        builder.append("end");

        return null;
    }

    @Override
    public Void visitClassConstructor(ClassConstructor c) {
        builder.append("new(");
        prettyPrintParameters(c.parameters());
        builder.append(") ");
        c.body().accept(this);
        builder.append(";");

        return null;
    }

    @Override
    public Void visitEmptyStatement(EmptyStatement e) {
        builder.append(";");
        return null;
    }
    @Override
    public Void visitExpressionStatement(ExpressionStatement e) {
        e.expression().accept(this);
        builder.append(";");
        return null;
    }
    @Override
    public Void visitIfStatement(IfStatement i) {
        builder.append("if ");
        i.condition().accept(this);
        builder.append(" do ");
        i.body().accept(this);
        
        return null;
    }
    @Override
    public Void visitReturnExpression(ReturnExpression r) {
        builder.append("return ");
        r.returnValue().accept(this);

        return null;
    }

    @Override
    public Void visitSetStatement(SetStatement s) {
        s.setter().accept(this);
        builder.append(" := ");
        s.expression().accept(this);
        builder.append(";");
        
        return null;
    }

   @Override
    public Void visitEnumDeclaration(EnumDeclaration e) {
        builder.append("enum ");
        builder.append(e.Name().lexeme());
        builder.append(" where ");
        for(var variant : e.variants()) {
            variant.accept(this);
        }
        builder.append(" end");

        return null;
    }
    @Override
    public Void visitEnumConstructor(EnumConstructor e) {
        builder.append(e.name().lexeme());
        builder.append("(");
        boolean needComma = false;
        for(var param: e.parameters()) {
            if(needComma) builder.append(", ");
            param.accept(this);
        }
        builder.append(");");

        return null;
    }
    @Override
    public Void visitEnumType(EnumType e) {
        builder.append(e.name().lexeme());
        return null;
    }

    @Override
    public Void visitMatchExpression(MatchExpression m) {
        builder.append("match ");
        m.matched().accept(this);
        builder.append(" do ");
        for(int i = 0; i < m.cases().size(); i++) {
            builder.append("case ");
            m.cases().get(i).accept(this);
            builder.append(" := ");
            m.branches().get(i).accept(this);
            builder.append("; ");
        }
        builder.append("end");

        return null;
    }

    @Override
    public Void visitWildcard(Wildcard w) {
        builder.append("?");
        return null;
    }

    @Override
    public Void visitNumberPattern(NumberPattern n) {
        builder.append(n.value());
        return null;
    }
    @Override
    public Void visitBooleanPattern(BooleanPattern b) {
        builder.append(b.value());
        return null;
    }
    @Override
    public Void visitStringPattern(StringPattern s) {
        builder.append("\"");
        builder.append(s.value().replace("\"", "\\\""));
        builder.append("\"");

        return null;
    }

    @Override
    public Void visitVariableBinding(VariableBinding v) {
        builder.append(v.name());
        return null;
    }
    @Override
    public Void visitConstructorPattern(ConstructorPattern c) {
        builder.append(c.constr().lexeme());
        builder.append("(");
        boolean needsComma = false;
        for(var pat: c.subPatterns()) {
            if(needsComma) builder.append(", ");
            pat.accept(this);
        }
        builder.append(")");
        return null;
    }
    @Override
    public Void visitNever(Never n) {
        builder.append("Never");
        return null;
    }
    @Override
    public Void visitTupleExpression(TupleExpression t) {
        builder.append("(");
        boolean needComma = false;
        for(var expr : t.elements()) {
            if(needComma) builder.append(", ");
            expr.accept(this);
            needComma = true;
        }
        builder.append(")");
        return null;
    }
    @Override
    public Void visitTuple(Tuple t) {
        builder.append("(");
        boolean needComma = false;
        for(var type: t.types()) {
            if(needComma) builder.append(", ");
            type.accept(this);
            needComma = true;
        }
        builder.append(")");
        return null;
    }
    @Override
    public Void visitTuplePattern(TuplePattern p) {
        builder.append("(");
        boolean needComma = false;
        for(var pat: p.subPatterns()) {
            if(needComma) builder.append(", ");
            pat.accept(this);
            needComma = true;
        }
        builder.append(")");
        return null;
    }
    @Override
    public Void visitTupleRep(TupleRep t) {
        builder.append("(");
        boolean needComma = false;
        for(var typerep : t.elements()) {
            if(needComma) builder.append(", ");
            typerep.accept(this);
            needComma = true;
        }
        builder.append(")");
        return null;
    }
    @Override
    public Void visitWildcardExpression(WildcardExpression w) {
        builder.append("?");
        return null;
    }
    @Override
    public Void visitWildcardSetter(WildcardSetter w) {
        builder.append("?");
        return null;
    }
    @Override
    public Void visitVariableSetter(VariableSetter v) {
        builder.append(v.name().lexeme());
        return null;
    }
    @Override
    public Void visitIndexSetter(IndexSetter i) {
        builder.append("(");
        i.list().accept(this);
        builder.append(")[");
        i.index().accept(this);
        builder.append("]");
        return null;
    }
    @Override
    public Void visitPropertySetter(PropertySetter p) {
        builder.append("(");
        p.object().accept(this);
        builder.append(").");
        builder.append(p.name().lexeme());
        return null;
    }
    @Override
    public Void visitTupleSetter(TupleSetter s) {
        builder.append("(");
        boolean needComma = false;
        for(var setter: s.setters()) {
            if(needComma) builder.append(", ");
            setter.accept(this);
        }
        builder.append(")");
        return null;
    }
 }
