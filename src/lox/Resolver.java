package lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Stmt.Visitor<Void>, Expr.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private enum FunctionType {
        // TODO: add more types for classes later
        NONE,
        FUNCTION
    }

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    // ------------- START RESOLUTION HELPERS ------------- //
    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(List<Stmt> stmtList) {
        for (Stmt stmt : stmtList) {
            resolve(stmt);
        }
    }

    /** This is where all resolutions end up and exit to the interpreter **/
    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - i - 1);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        beginScope();

        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);

        currentFunction = enclosingFunction;
        endScope();
    }
    // ------------- END RESOLUTION HELPERS ------------- //

    // ------------- START SCOPING HELPERS ------------- //
    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Cannot redeclare variable in the same scope.");
        }

        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        scope.put(name.lexeme, true);
    }
    // ------------- END SCOPING HELPERS ------------- //

    // ------------- START EXPRESSION RESOLVERS ------------- //
    @Override
    public Void visitBinaryExpr(Expr.Binary binary) {
        resolve(binary.left);
        resolve(binary.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call call) {
        resolve(call.callee);
        for (Expr arg : call.arguments) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping grouping) {
        resolve(grouping.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal literal) {
        // literals have no sub-expressions to resolve
        // so nothing is done here
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary unary) {
        resolve(unary.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable variable) {
        Token name = variable.name;

        // prevents weird things like a local declaration of "var a = a"
        if (!scopes.empty() && !scopes.peek().get(name.lexeme)) {
            Lox.error(name, "Can't read local variable in its own initializer.");
        }

        resolveLocal(variable, name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign assign) {
        resolve(assign.value);
        resolveLocal(assign, assign.name);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical logical) {
        resolve(logical.left);
        resolve(logical.right);
        return null;
    }
    // ------------- END EXPRESSION RESOLVERS ------------- //

    // ------------- START STATEMENT RESOLVERS ------------- //
    @Override
    public Void visitExpressionStmt(Stmt.Expression expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print print) {
        resolve(print.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var var) {
        declare(var.name);
        if (var.initializer != null) {
            resolve(var.initializer);
        }
        define(var.name);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return Return) {
        if (currentFunction != FunctionType.FUNCTION) {
            // user puts a return statement outside of a fxn
            Lox.error(Return.keyword, "Cannot return from top-level code.");
        }

        if (Return.value != null) resolve(Return.value);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function function) {
        // function name is eagerly resolved in outer scope
        declare(function.name);
        define(function.name);

        resolveFunction(function, FunctionType.FUNCTION);
        return null;
    }


    @Override
    public Void visitBlockStmt(Stmt.Block block) {
        beginScope();
        resolve(block.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If If) {
        resolve(If.condition);
        resolve(If.thenBranch);

        if (If.elseBranch != null) {
            resolve(If.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While While) {
        resolve(While.condition);
        resolve(While.body);
        return null;
    }
    // ------------- END STATEMENT RESOLVERS ------------- //
}
