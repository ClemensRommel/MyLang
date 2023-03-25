package MyLang;
import java.util.List;
import java.util.Map;
public interface MyLangAST {
public static interface ExpressionVisitor<T> {
public T visitNumericLiteral(NumericLiteral value);
public T visitStringLiteral(StringLiteral value);
public T visitBooleanLiteral(BooleanLiteral value);
public T visitNullLiteral(NullLiteral value);
public T visitIdentifier(Identifier value);
public T visitBinaryOperation(BinaryOperation value);
public T visitUnaryOperation(UnaryOperation value);
public T visitFunctionCall(FunctionCall value);
public T visitFunctionExpression(FunctionExpression value);
public T visitIfExpression(IfExpression value);
public T visitListExpression(ListExpression value);
public T visitIndexExpression(IndexExpression value);
public T visitPropertyExpression(PropertyExpression value);
public T visitBlockExpression(BlockExpression value);
public T visitWhileYieldExpression(WhileYieldExpression value);
public T visitWhileDoExpression(WhileDoExpression value);
public T visitForYieldExpression(ForYieldExpression value);
public T visitForDoExpression(ForDoExpression value);
public T visitRangeExpression(RangeExpression value);
public T visitThisExpression(ThisExpression value);
public T visitNameSpaceExpression(NameSpaceExpression value);
}
public static sealed interface Expression extends MyLangAST {
    public <T> T accept(ExpressionVisitor<T> visitor);
}
public static record NumericLiteral(double value) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitNumericLiteral(this);
}}
public static record StringLiteral(String value) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitStringLiteral(this);
}}
public static record BooleanLiteral(boolean value) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitBooleanLiteral(this);
}}
public static record NullLiteral(Token keyword) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitNullLiteral(this);
}}
public static record Identifier(Token value) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitIdentifier(this);
}}
public static record BinaryOperation(Token operator, Expression left, Expression right) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitBinaryOperation(this);
}}
public static record UnaryOperation(Token operator, Expression operand) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitUnaryOperation(this);
}}
public static record FunctionCall(Expression callee, List<Parameter> arguments) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitFunctionCall(this);
}}
public static record FunctionExpression(String optionalName, List<Token> parameters, Token varargsName, Expression body) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitFunctionExpression(this);
}}
public static record IfExpression(Expression condition, Expression thenBranch, Expression elseBranch) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitIfExpression(this);
}}
public static record ListExpression(List<Parameter> elements) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitListExpression(this);
}}
public static record IndexExpression(Expression list, Expression index) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitIndexExpression(this);
}}
public static record PropertyExpression(Expression object, Token name) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitPropertyExpression(this);
}}
public static record BlockExpression(List<DeclarationOrStatement> statements, Expression returnValue) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitBlockExpression(this);
}}
public static record WhileYieldExpression(Expression condition, Parameter body) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitWhileYieldExpression(this);
}}
public static record WhileDoExpression(Expression condition, Statement body) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitWhileDoExpression(this);
}}
public static record ForYieldExpression(Token variable, Expression collection, Expression guard, Parameter body) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitForYieldExpression(this);
}}
public static record ForDoExpression(Token variable, Expression collection, Expression guard, Statement body) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitForDoExpression(this);
}}
public static record RangeExpression(Expression start, Expression end, Expression step) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitRangeExpression(this);
}}
public static record ThisExpression(Token keyword) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitThisExpression(this);
}}
public static record NameSpaceExpression(MyLangPath nameSpace, Token name) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitNameSpaceExpression(this);
}}
public static interface DeclarationVisitor<T> {
public T visitVariableDeclaration(VariableDeclaration value);
public T visitFunctionDeclaration(FunctionDeclaration value);
public T visitClassDeclaration(ClassDeclaration value);
public T visitModuleDeclaration(ModuleDeclaration value);
public T visitImportDeclaration(ImportDeclaration value);
}
public static sealed interface Declaration extends MyLangAST,  DeclarationOrStatement,  ConstructorOrDeclaration {
    public <T> T accept(DeclarationVisitor<T> visitor);
}
public static record VariableDeclaration(Token Name, Expression initializer, boolean isReassignable, boolean export) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitVariableDeclaration(this);
}}
public static record FunctionDeclaration(Token Name, List<Token> parameters, Token varargsName, Expression body, boolean export) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitFunctionDeclaration(this);
}}
public static record ClassDeclaration(Token Name, List<Declaration> fieldsAndMethods, ClassConstructor constructor, boolean export) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitClassDeclaration(this);
}}
public static record ModuleDeclaration(MyLangPath Name) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitModuleDeclaration(this);
}}
public static record ImportDeclaration(MyLangPath Name) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitImportDeclaration(this);
}}
public static interface StatementVisitor<T> {
public T visitExpressionStatement(ExpressionStatement value);
public T visitSetStatement(SetStatement value);
public T visitSetIndexStatement(SetIndexStatement value);
public T visitSetPropertyStatement(SetPropertyStatement value);
}
public static sealed interface Statement extends MyLangAST,  DeclarationOrStatement {
    public <T> T accept(StatementVisitor<T> visitor);
}
public static record ExpressionStatement(Expression expression) implements Statement {
public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitExpressionStatement(this);
}}
public static record SetStatement(Token name, Expression expression) implements Statement {
public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitSetStatement(this);
}}
public static record SetIndexStatement(Expression list, Expression index, Expression expression) implements Statement {
public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitSetIndexStatement(this);
}}
public static record SetPropertyStatement(Expression target, Token name, Expression expression) implements Statement {
public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitSetPropertyStatement(this);
}}
public static interface ConstructorVisitor<T> {
public T visitClassConstructor(ClassConstructor value);
}
public static sealed interface Constructor extends MyLangAST,  ConstructorOrDeclaration {
    public <T> T accept(ConstructorVisitor<T> visitor);
}
public static record ClassConstructor(Token keyword, List<Token> parameters,Token varargsName, Expression body) implements Constructor {
public <T> T accept(ConstructorVisitor<T> visitor) {
    return visitor.visitClassConstructor(this);
}}
public static interface ParameterVisitor<T> {
public T visitExpressionParameter(ExpressionParameter value);
public T visitSpreadParameter(SpreadParameter value);
public T visitConditionalParameter(ConditionalParameter value);
}
public static sealed interface Parameter extends MyLangAST {
    public <T> T accept(ParameterVisitor<T> visitor);
}
public static record ExpressionParameter(Expression expr) implements Parameter {
public <T> T accept(ParameterVisitor<T> visitor) {
    return visitor.visitExpressionParameter(this);
}}
public static record SpreadParameter(Expression collection) implements Parameter {
public <T> T accept(ParameterVisitor<T> visitor) {
    return visitor.visitSpreadParameter(this);
}}
public static record ConditionalParameter(Expression body, Expression guard) implements Parameter {
public <T> T accept(ParameterVisitor<T> visitor) {
    return visitor.visitConditionalParameter(this);
}}
public static sealed interface ConstructorOrDeclaration extends MyLangAST {
}
public static sealed interface DeclarationOrStatement extends MyLangAST {
}
}
