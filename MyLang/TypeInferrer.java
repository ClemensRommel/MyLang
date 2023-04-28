package MyLang;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static MyLang.MyLangAST.*;

public class TypeInferrer 
    implements ExpressionVisitor<TypeRep> {
    private Typechecker tc;

    public TypeInferrer(Typechecker t) {
        tc = t;
    }

    public TypeRep infer(Expression e) {
        return e.accept(this);
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
            if(m.enviroment().valueExported(p.name().lexeme()) && m.enviroment().valueExists(p.name().lexeme())) {
                return m.enviroment().getTypeOfValue(p.name().lexeme());
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
        return tc.inferElemTypeOfParameter(wy.body(), true);
    }

    @Override
    public TypeRep visitForDoExpression(ForDoExpression fd) {
        return Typechecker.voidType;
    }
    @Override
    public TypeRep visitForYieldExpression(ForYieldExpression fy) {
        var collectionType = infer(fy.collection());
        tc.openScope();
        if(collectionType instanceof ListOfRep l) {
            tc.declareType(fy.variable().lexeme(), l.elements(), false);
        }
        var resultingType = tc.inferElemTypeOfParameter(fy.body(), true);
        tc.closeScope();
        return resultingType;
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
        return infer(m.branches().get(0));
    }
}
