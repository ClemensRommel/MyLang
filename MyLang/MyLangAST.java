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
public static record FunctionCall(Expression callee, List<Parameter> arguments, Map<String, Expression> named) implements Expression {
public <T> T accept(ExpressionVisitor<T> visitor) {
    return visitor.visitFunctionCall(this);
}}
public static record FunctionExpression(String optionalName, ParameterInformation parameters, Expression body, Type retType) implements Expression {
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
public static interface DeclarationVisitor<T> {
public T visitVariableDeclaration(VariableDeclaration value);
public T visitFunctionDeclaration(FunctionDeclaration value);
public T visitClassDeclaration(ClassDeclaration value);
public T visitEnumDeclaration(EnumDeclaration value);
public T visitTypeDefDeclaration(TypeDefDeclaration value);
public T visitModuleDeclaration(ModuleDeclaration value);
public T visitEmptyDeclaration(EmptyDeclaration value);
}
public static sealed interface Declaration extends MyLangAST,  DeclarationOrStatement,  ConstructorOrDeclaration {
    public <T> T accept(DeclarationVisitor<T> visitor);
}
public static record VariableDeclaration(Token Name, Type type, Expression initializer, boolean isReassignable, boolean export) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitVariableDeclaration(this);
}}
public static record FunctionDeclaration(Token Name, ParameterInformation parameters, Expression body, Type retType, boolean export) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitFunctionDeclaration(this);
}}
public static record ClassDeclaration(Token Name, List<Declaration> fieldsAndMethods, ClassConstructor constructor, boolean export) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitClassDeclaration(this);
}}
public static record EnumDeclaration(Token Name, List<EnumConstructor> variants, boolean export) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitEnumDeclaration(this);
}}
public static record TypeDefDeclaration(Token Name, Type definition, boolean export) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitTypeDefDeclaration(this);
}}
public static record ModuleDeclaration(MyLangPath Name) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitModuleDeclaration(this);
}}
public static record EmptyDeclaration(Token semicolon) implements Declaration {
public <T> T accept(DeclarationVisitor<T> visitor) {
    return visitor.visitEmptyDeclaration(this);
}}
public static interface ImportVisitor<T> {
public T visitImportDeclaration(ImportDeclaration value);
}
public static sealed interface Import extends MyLangAST {
    public <T> T accept(ImportVisitor<T> visitor);
}
public static record ImportDeclaration(MyLangPath Name) implements Import {
public <T> T accept(ImportVisitor<T> visitor) {
    return visitor.visitImportDeclaration(this);
}}
public static interface StatementVisitor<T> {
public T visitExpressionStatement(ExpressionStatement value);
public T visitIfStatement(IfStatement value);
public T visitSetStatement(SetStatement value);
public T visitSetIndexStatement(SetIndexStatement value);
public T visitSetPropertyStatement(SetPropertyStatement value);
public T visitReturnStatement(ReturnStatement value);
public T visitEmptyStatement(EmptyStatement value);
}
public static sealed interface Statement extends MyLangAST,  DeclarationOrStatement {
    public <T> T accept(StatementVisitor<T> visitor);
}
public static record ExpressionStatement(Expression expression) implements Statement {
public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitExpressionStatement(this);
}}
public static record IfStatement(Expression condition, Statement body) implements Statement {
public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitIfStatement(this);
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
public static record ReturnStatement(Expression returnValue) implements Statement {
public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitReturnStatement(this);
}}
public static record EmptyStatement(Token semicolon) implements Statement {
public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitEmptyStatement(this);
}}
public static interface ConstructorVisitor<T> {
public T visitClassConstructor(ClassConstructor value);
public T visitEnumConstructor(EnumConstructor value);
}
public static sealed interface Constructor extends MyLangAST,  ConstructorOrDeclaration {
    public <T> T accept(ConstructorVisitor<T> visitor);
}
public static record ClassConstructor(Token keyword, ParameterInformation parameters, Expression body) implements Constructor {
public <T> T accept(ConstructorVisitor<T> visitor) {
    return visitor.visitClassConstructor(this);
}}
public static record EnumConstructor(Token name, List<Type> parameters) implements Constructor {
public <T> T accept(ConstructorVisitor<T> visitor) {
    return visitor.visitEnumConstructor(this);
}}
public static interface ParameterVisitor<T> {
public T visitExpressionParameter(ExpressionParameter value);
public T visitSpreadParameter(SpreadParameter value);
public T visitNamedParameter(NamedParameter value);
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
public static record NamedParameter(Token name, Expression parameter) implements Parameter {
public <T> T accept(ParameterVisitor<T> visitor) {
    return visitor.visitNamedParameter(this);
}}
public static record ConditionalParameter(Expression body, Expression guard) implements Parameter {
public <T> T accept(ParameterVisitor<T> visitor) {
    return visitor.visitConditionalParameter(this);
}}
public static interface PatternVisitor<T> {
public T visitVariableBinding(VariableBinding value);
public T visitWildcard(Wildcard value);
public T visitNumberPattern(NumberPattern value);
public T visitBooleanPattern(BooleanPattern value);
public T visitStringPattern(StringPattern value);
public T visitConstructorPattern(ConstructorPattern value);
}
public static sealed interface Pattern extends MyLangAST {
    public <T> T accept(PatternVisitor<T> visitor);
}
public static record VariableBinding(Token name) implements Pattern {
public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitVariableBinding(this);
}}
public static record Wildcard() implements Pattern {
public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitWildcard(this);
}}
public static record NumberPattern(double value) implements Pattern {
public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitNumberPattern(this);
}}
public static record BooleanPattern(boolean value) implements Pattern {
public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitBooleanPattern(this);
}}
public static record StringPattern(String value) implements Pattern {
public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitStringPattern(this);
}}
public static record ConstructorPattern(Token constr, List<Pattern> subPatterns) implements Pattern {
public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitConstructorPattern(this);
}}
public static sealed interface ConstructorOrDeclaration extends MyLangAST {
}
public static sealed interface DeclarationOrStatement extends MyLangAST {
}
public static interface TypeVisitor<T> {
public T visitTypeIdentifier(TypeIdentifier value);
public T visitFunctionType(FunctionType value);
public T visitListOf(ListOf value);
public T visitAccess(Access value);
public T visitVoidType(VoidType value);
public T visitNumberType(NumberType value);
public T visitBooleanType(BooleanType value);
public T visitStringType(StringType value);
}
public static sealed interface Type extends MyLangAST {
    public <T> T accept(TypeVisitor<T> visitor);
}
public static record TypeIdentifier(Token name) implements Type {
public <T> T accept(TypeVisitor<T> visitor) {
    return visitor.visitTypeIdentifier(this);
}}
public static record FunctionType(List<Type> parameters, 
            List<Type> optionalParameters, 
            Map<String, Type> named, 
            Map<String, Type> optionalNamed,
            Type varargsType, 
            Type returnType) implements Type {
public <T> T accept(TypeVisitor<T> visitor) {
    return visitor.visitFunctionType(this);
}}
public static record ListOf(Type elements) implements Type {
public <T> T accept(TypeVisitor<T> visitor) {
    return visitor.visitListOf(this);
}}
public static record Access(Type accessed, Token name) implements Type {
public <T> T accept(TypeVisitor<T> visitor) {
    return visitor.visitAccess(this);
}}
public static record VoidType() implements Type {
public <T> T accept(TypeVisitor<T> visitor) {
    return visitor.visitVoidType(this);
}}
public static record NumberType() implements Type {
public <T> T accept(TypeVisitor<T> visitor) {
    return visitor.visitNumberType(this);
}}
public static record BooleanType() implements Type {
public <T> T accept(TypeVisitor<T> visitor) {
    return visitor.visitBooleanType(this);
}}
public static record StringType() implements Type {
public <T> T accept(TypeVisitor<T> visitor) {
    return visitor.visitStringType(this);
}}
public static interface TypeRepVisitor<T> {
public T visitTypeIdentifierRep(TypeIdentifierRep value);
public T visitFunctionTypeRep(FunctionTypeRep value);
public T visitClassType(ClassType value);
public T visitEnumType(EnumType value);
public T visitListOfRep(ListOfRep value);
public T visitBuiltin(Builtin value);
public T visitAccessRep(AccessRep value);
public T visitUnknownType(UnknownType value);
public T visitModule(Module value);
}
public static sealed interface TypeRep extends MyLangAST {
    public <T> T accept(TypeRepVisitor<T> visitor);
}
public static record TypeIdentifierRep(Token name, TypeEnv env) implements TypeRep {
public <T> T accept(TypeRepVisitor<T> visitor) {
    return visitor.visitTypeIdentifierRep(this);
}}
public static record FunctionTypeRep(List<TypeRep> parameters, 
            List<TypeRep> optionalParameters, 
            Map<String, TypeRep> named, 
            Map<String, TypeRep> optionalNamed,
            TypeRep varargsType, 
            TypeRep returnType, 
            TypeEnv env) implements TypeRep {
public <T> T accept(TypeRepVisitor<T> visitor) {
    return visitor.visitFunctionTypeRep(this);
}}
public static record ClassType(Token name, 
            Map<String, TypeRep> accessors, 
            Map<String, Boolean> readability, 
            FunctionTypeRep constructor, 
            TypeEnv env) implements TypeRep {
public <T> T accept(TypeRepVisitor<T> visitor) {
    return visitor.visitClassType(this);
}}
public static record EnumType(Token name,
            Map<String, TypeRep> variants,
            TypeEnv  env) implements TypeRep {
public <T> T accept(TypeRepVisitor<T> visitor) {
    return visitor.visitEnumType(this);
}}
public static record ListOfRep(TypeRep elements) implements TypeRep {
public <T> T accept(TypeRepVisitor<T> visitor) {
    return visitor.visitListOfRep(this);
}}
public static record Builtin(BuiltinType type) implements TypeRep {
public <T> T accept(TypeRepVisitor<T> visitor) {
    return visitor.visitBuiltin(this);
}}
public static record AccessRep(TypeRep accessed, Token name) implements TypeRep {
public <T> T accept(TypeRepVisitor<T> visitor) {
    return visitor.visitAccessRep(this);
}}
public static record UnknownType() implements TypeRep {
public <T> T accept(TypeRepVisitor<T> visitor) {
    return visitor.visitUnknownType(this);
}}
public static record Module(String name, TypeEnv enviroment) implements TypeRep {
public <T> T accept(TypeRepVisitor<T> visitor) {
    return visitor.visitModule(this);
}}
}
