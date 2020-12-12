package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    /** holds pre-defined native functions **/
    final Environment GLOBALS = new Environment();
    Environment environment = GLOBALS;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        GLOBALS.define("clock", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal literal) {
        return literal.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping grouping) {
        return evaluate(grouping.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary unary) {
        Object right = evaluate(unary.right);

        switch (unary.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(unary.operator, right);
                return - (double) right;
        default:
            return null;
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary binary) {
        Object left = evaluate(binary.left);
        Object right = evaluate(binary.right);

        switch (binary.operator.type) {
        // arithmetic operators
        case MINUS:
            checkNumberOperands(binary.operator, left, right);
            return (double) left - (double) right;
        case PLUS:
            // string concatenation
            if (left instanceof String && right instanceof String) {
                return (String) left + (String) right;
            }
            if (left instanceof Double && right instanceof Double) {
                return (double) left + (double) right;
            }

            throw new RuntimeError(binary.operator, "Operands must be either numbers or strings.");
        case STAR:
            checkNumberOperands(binary.operator, left, right);
            return (double) left * (double) right;
        case SLASH:
            checkNumberOperands(binary.operator, left, right);
            return (double) left / (double) right;
        // comparison operators
        case GREATER:
            checkNumberOperands(binary.operator, left, right);
            return (double) left > (double) right;
        case GREATER_EQUAL:
            checkNumberOperands(binary.operator, left, right);
            return (double) left >= (double) right;
        case LESS:
            checkNumberOperands(binary.operator, left, right);
            return (double) left < (double) right;
        case LESS_EQUAL:
            checkNumberOperands(binary.operator, left, right);
            return (double) left <= (double) right;
        // equality operators
        case EQUAL_EQUAL:
            return isEqual(left, right);
        case BANG_EQUAL:
            return !isEqual(left, right);
        default:
            return null;
        }
    }

    @Override
    public Object visitCallExpr(Expr.Call call) {
        //--------- process callee ----------//
        Object callee = evaluate(call.callee);

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(call.paren, "Only functions and classes can be called.");
        }
        LoxCallable callable = (LoxCallable) callee;

        //--------- process args ----------//
        List<Object> arguments = new ArrayList<>();
        for (Expr arg : call.arguments) {
            arguments.add(evaluate(arg));
        }

        if (arguments.size() != callable.arity()) {
            throw new RuntimeError(call.paren, "Expected " + callable.arity() + " arguments but got " +
                                          arguments.size() + ".");
        }

        return callable.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get get) {
        Object object = evaluate(get.object);

        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(get.name, "Only instances can have properties.");
        }

        LoxInstance instance = (LoxInstance) object;
        return instance.get(get.name);
    }

    @Override
    public Object visitSetExpr(Expr.Set set) {
        Object object = evaluate(set.object);

        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(set.name, "Only instances can have properties.");
        }

        Object value = evaluate(set.value);
        LoxInstance instance = (LoxInstance) object;
        instance.set(set.name, value);
        return value;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable variable) {
        return lookUpVariable(variable.name, variable);
    }

    private Object lookUpVariable(Token name, Expr var) {
        Integer distance = locals.get(var);
        if (distance == null) {
            return GLOBALS.get(name);
        }

        return environment.getAt(distance, name);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign assign) {
        Object value = evaluate(assign.value);

        Integer distance = locals.get(assign);
        if (distance == null) {
            GLOBALS.assign(assign.name, value);
        } else {
            environment.assignAt(distance, assign.name, value);
        }

        return value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical logical) {
        Object left = evaluate(logical.left);
        boolean isLeftTruthy = isTruthy(left);

        if (logical.operator.type == TokenType.OR)  {
            return isLeftTruthy ? left : evaluate(logical.right);
        } else {
            return isLeftTruthy ? evaluate(logical.right) : left;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression expressionStmt) {
        evaluate(expressionStmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print printStmt) {
        Object result = evaluate(printStmt.expression);
        System.out.println(stringify(result));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return returnStmt) {
        Object value = returnStmt.value == null
                   ? null
                   : evaluate(returnStmt.value);
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var varStmt) {
        Object value = null;
        if (varStmt.initializer != null) {
            value = evaluate(varStmt.initializer);
        }

        environment.define(varStmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class classStmt) {
        environment.define(classStmt.name.lexeme, null);
        LoxClass klass = new LoxClass(classStmt.name.lexeme);
        environment.assign(classStmt.name, klass);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function functionStmt) {
        LoxFunction function = new LoxFunction(functionStmt, environment);
        environment.define(functionStmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block blockStmt) {
        executeBlock(blockStmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        // retain a copy of the outer environment to restore after the block
        Environment original = this.environment;

        try {
            // statements within a block are interpreted in a new local env
            this.environment = environment;
            interpret(statements);
        } finally {
            this.environment = original;
        }
    }

    @Override
    public Void visitIfStmt(Stmt.If ifStmt) {
        boolean condition = isTruthy(evaluate(ifStmt.condition));
        if (condition) {
            execute(ifStmt.thenBranch);
        } else if (ifStmt.elseBranch != null) {
            execute(ifStmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While whileStmt) {
        while (isTruthy(evaluate(whileStmt.condition))) {
            execute(whileStmt.body);
        }
        return null;
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private String stringify(Object obj) {
        if (obj == null) return "nil";

        if (obj instanceof Double) {
            String strRepresentation = obj.toString();
            if (strRepresentation.endsWith(".0")) {
                // trim and display as integer
                return strRepresentation.substring(0, strRepresentation.length() - 2);
            }
        }

        return obj.toString();
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean) value;
        return true;
    }

    private boolean isEqual(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 == null) return false;
        return o1.equals(o2);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object operand1, Object operand2) {
        if (operand1 instanceof Double && operand2 instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be a number.");
    }
}
