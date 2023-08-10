package MyLang;

import static MyLang.MyLangAST.*;

public class TypeInferrer 
    implements ExpressionVisitor<TypeRep>, PatternVisitor<Void>, SetterVisitor<TypeRep> {
    private Typechecker tc;
    private TypeRep matched = null;

    public TypeInferrer(Typechecker t) {
        tc = t;
    }

    public TypeRep infer(Expression e) {
        return e.accept(this);
    }

    public TypeRep inferSetter(Setter s) {
        return s.accept(this);
    }

    private void inferPatternTypes(Pattern p, TypeRep matched) {
        var previousMatched = this.matched;
        this.matched = tc.env.normalize(matched, tc);
        p.accept(this);
        this.matched = previousMatched;
    }

    @Override
    public TypeRep visitNumericLiteral(NumericLiteral n) {
        return Typechecker.numberType;
    }

    @Override
    public TypeRep visitStringLiteral(StringLiteral s) {
        return Typechecker.stringType;   
    }
    @Override
    public TypeRep visitBooleanLiteral(BooleanLiteral b) {
        return Typechecker.booleanType;
    }
    @Override
    public TypeRep visitNullLiteral(NullLiteral n) {
        return Typechecker.voidType;
    }

    @Override
    public TypeRep visitIdentifier(Identifier i) {
        return tc.getTypeOf(i.value(), true);
    }

    @Override
    public TypeRep visitBinaryOperation(BinaryOperation b) {
        return switch(b.operator().type()) {
            case AND, OR, XOR -> Typechecker.booleanType;
            case PLUS -> {
                var leftType = infer(b.left());
                if(leftType.equals(Typechecker.stringType)) {
                    yield Typechecker.stringType;
                } else {
                    var rightType = infer(b.right());
                    if(rightType.equals(Typechecker.stringType)) {
                        yield Typechecker.stringType;
                    } else {
                        yield Typechecker.numberType;
                    }
                }
            }
            case MINUS, STAR, SLASH, PERCENT, EXPO -> Typechecker.numberType;
            case LESS, LESS_EQUAL, EQUAL, GREATER_EQUAL, GREATER, IN -> Typechecker.booleanType;
            default -> {System.out.println("Invalid binary Operator"); yield Typechecker.unknown();}
        };
    }
    @Override
    public TypeRep visitUnaryOperation(UnaryOperation u) {
        return switch(u.operator().type()) {
            case BANG -> Typechecker.booleanType;
            case PLUS, MINUS -> Typechecker.numberType;
            default -> {System.out.println("Invalid unary Operator");yield Typechecker.unknown();}
        };
    }

    @Override
    public TypeRep visitFunctionCall(FunctionCall c) {
        var funType = infer(c.callee());
        if(funType instanceof FunctionTypeRep t) {
            return t.returnType();
        } else {
            return Typechecker.unknown();
        }
    }

    @Override
    public TypeRep visitFunctionExpression(FunctionExpression f) {
        if(f.parameters().types().stream().anyMatch(x -> x == null) || f.retType() == null) {
            tc.error("Cannot infer types of parameters or returntype of function expression "+tc.p.prettyPrint(f));
        }
        return new FunctionTypeRep(
                f.parameters().types().stream().map(tc.tcomp::compileType).toList(),
                f.parameters().optionals().stream().map(OptionalParam::type).map(tc.tcomp::compileType).toList(),
                tc.tcomp.namedTypesIn(f.parameters().named()),
                tc.tcomp.optionalNamedTypesIn(f.parameters().optionalNamed()),
                tc.tcomp.compileType(f.parameters().varargsType()),
                tc.tcomp.compileType(f.retType()),
                tc.env);
    }

    @Override
    public TypeRep visitIfExpression(IfExpression i) {
        return infer(i.thenBranch());
    }
    @Override
    public TypeRep visitIfValExpression(IfValExpression i) {
        tc.openScope();
            inferPatternTypes(i.pat(), infer(i.matched()));
            var thenBranchType = infer(i.thenBranch());
        tc.closeScope();
        return thenBranchType;
    }

    @Override
    public TypeRep visitListExpression(ListExpression l) {
        if(l.elements().size() == 0) {
            tc.error("Cannot infer type of empty list");
            return new ListOfRep(Typechecker.unknown());
        }
        return new ListOfRep(tc.inferElemTypeOfParameter(l.elements().get(0), true));
    }
    @Override 
    public TypeRep visitIndexExpression(IndexExpression i) {
        var listType = infer(i.list());
        if(listType instanceof ListOfRep l) {
            return l.elements();
        } else if(listType instanceof Builtin b && b.type().equals(BuiltinType.STRING)) {
            return b;
        } else {
            return Typechecker.unknown();
        }
    }

    @Override
    public TypeRep visitPropertyExpression(PropertyExpression p) {
        var classType = tc.env.normalize(infer(p.object()), tc);
        if(classType instanceof ClassType t) {
            if(t.accessors().containsKey(p.name().lexeme())) {
                return t.accessors().get(p.name().lexeme());
            } else {
                return Typechecker.unknown();
            }
        } else if(classType instanceof ListOfRep l) {
            //System.out.println("Inferring type of list method "+p.name().lexeme());
            return tc.listMethodType(p.name().lexeme(), l);
        } else if(classType instanceof MyLangAST.Module m) {
            //if(m.enviroment().valueExported(p.name().lexeme()) && m.enviroment().valueExists(p.name().lexeme())) {
                return m.enviroment().getTypeOfValue(p.name().lexeme());
            /*} else {
                return Typechecker.unknown();
            }*/
        } else if(classType instanceof EnumType e) {
            if(e.methods().containsKey(p.name().lexeme())) {
                return e.methods().get(p.name().lexeme());
            } else {
                return Typechecker.unknown();
            }
        } else {
            return Typechecker.unknown();
        }
    }

    @Override
    public TypeRep visitBlockExpression(BlockExpression b) {
        tc.openScope();
        for(var decl: b.statements()) {
            tc.declare(decl);
        }

        var resultingType = infer(b.returnValue());
        tc.closeScope();
        return resultingType;
    }

    @Override
    public TypeRep visitWhileDoExpression(WhileDoExpression wd) {
        return Typechecker.voidType;
    }

    @Override 
    public TypeRep visitWhileYieldExpression(WhileYieldExpression wy) {
        return new ListOfRep(tc.inferElemTypeOfParameter(wy.body(), true));
    }

    @Override
    public TypeRep visitWhileValDoExpression(WhileValDoExpression w) {
        return Typechecker.voidType;
    }
    @Override
    public TypeRep visitWhileValYieldExpression(WhileValYieldExpression wy) {
        tc.openScope();
            inferPatternTypes(wy.pattern(), infer(wy.matched()));
            var bodyType = infer(wy.body());
        tc.closeScope();
        return new ListOfRep(bodyType);
    }

    @Override
    public TypeRep visitForYieldExpression(ForYieldExpression fy) {
        var collectionType = infer(fy.collection());
        tc.openScope();
            if(collectionType instanceof ListOfRep l) {
                inferPatternTypes(fy.pat(), l.elements());
            }
            var resultingType = tc.inferElemTypeOfParameter(fy.body(), true);
        tc.closeScope();
        return new ListOfRep(resultingType);
    }

    @Override
    public TypeRep visitRangeExpression(RangeExpression r) {
        return Typechecker.rangeType;
    }

    @Override
    public TypeRep visitThisExpression(ThisExpression t) {
        if(!tc.exists("this")) return Typechecker.unknown();
        return tc.getTypeOf(t.keyword(), true);
    }

    @Override
    public TypeRep visitMatchExpression(MatchExpression m) {
        if(m.branches().size() == 0) {
            return Typechecker.unknown();
        }
        tc.openScope();
            inferPatternTypes(m.cases().get(0), infer(m.matched()));
            var branchType = infer(m.branches().get(0));
        tc.closeScope();
        return branchType;
    }
    @Override
    public Void visitWildcard(Wildcard w) {
        return null;
    }
    @Override
    public Void visitNumberPattern(NumberPattern p) {
        return null;
    }
    @Override 
    public Void visitStringPattern(StringPattern p) {
        return null;
    }
    @Override
    public Void visitBooleanPattern(BooleanPattern p) {
        return null;
    }
    @Override
    public Void visitVariableBinding(VariableBinding v) {
        tc.declareType(v.name().lexeme(), matched, false);
        return null;
    }
    @Override
    public Void visitConstructorPattern(ConstructorPattern p) {
        if(matched instanceof EnumType e) {
            if(e.variants().containsKey(p.constr().lexeme())) {
                var variant = e.variants().get(p.constr().lexeme());
                if(variant instanceof FunctionTypeRep f) {
                    if(f.parameters().size() == p.subPatterns().size()) {
                        for(int i = 0; i < p.subPatterns().size(); i++) {
                            inferPatternTypes(
                                p.subPatterns().get(i),
                                f.parameters().get(i));
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public TypeRep visitReturnExpression(ReturnExpression r) {
        return Typechecker.neverType;
    }
    @Override
    public TypeRep visitTupleExpression(TupleExpression t) {
        return new TupleRep(t.elements().stream().map(this::infer).toList());
    }
    @Override
    public Void visitTuplePattern(TuplePattern p) {
        if(matched instanceof TupleRep t) {
            var counter = Math.min(t.elements().size(), p.subPatterns().size());
            for(int i = 0; i < counter; i++) {
                inferPatternTypes(p.subPatterns().get(i), t.elements().get(i));
            }
        }
        return null;
    }
    @Override
    public TypeRep visitWildcardExpression(WildcardExpression w) {
        return Typechecker.unknown();
    }
    @Override
    public TypeRep visitWildcardSetter(WildcardSetter w) {
        return Typechecker.voidType;
    }
    @Override
    public TypeRep visitVariableSetter(VariableSetter v) {
        return tc.getTypeOf(v.name(), true);
    }
    @Override
    public TypeRep visitIndexSetter(IndexSetter i) {
        var listType = tc.env.normalize(infer(i.list()), tc);
        if(listType instanceof ListOfRep l) {
            return l.elements();
        } else {
            return Typechecker.unknown();
        }
    }
    @Override
    public TypeRep visitPropertySetter(PropertySetter p) {
        var objectType = tc.env.normalize(infer(p.object()), tc);
        if(objectType instanceof ClassType c) {
            if(c.accessors().containsKey(p.name().lexeme())) {
                return c.accessors().get(p.name().lexeme());
            }
        } else {
            System.out.println(objectType);
        }
        return Typechecker.unknown();
    }
    @Override
    public TypeRep visitTupleSetter(TupleSetter t) {
        return new TupleRep(t.setters().stream()
            .map(this::inferSetter)
            .toList()
        );
    }
    @Override
    public TypeRep visitInstExpression(InstExpression i) {
        var instantiated = infer(i.instantiated());
        TypeFunction tf;
        if(instantiated instanceof GenericType g) {
            tf = g.t();
        } else if(instantiated instanceof TypeFunction t) {
            tf = t; 
        } else {
            return Typechecker.unknown();
        }
        var resulting = tc.ta.apply(tf, i.args().stream()
            .map(tc.tcomp::compileType)
            .map(ta -> tc.env.normalize(ta, tc))
            .toList(), true);
        return resulting;
    }
}
