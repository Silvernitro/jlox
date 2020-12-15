package lox;

public class ASTPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary binary) {
        return parenthesize(binary.operator.lexeme, binary.left, binary.right);
    }

    @Override
    public String visitCallExpr(Expr.Call call) {
        // placeholder so that class can compile
        return null;
    }

    @Override
    public String visitGetExpr(Expr.Get Get) {
        // placeholder so that class can compile
        return null;
    }

    @Override
    public String visitSetExpr(Expr.Set Set) {
        // placeholder so that class can compile
        return null;
    }

    @Override
    public String visitSuperExpr(Expr.Super Super) {
        // placeholder so that class can compile
        return null;
    }

    @Override
    public String visitThisExpr(Expr.This This) {
        // placeholder so that class can compile
        return null;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping grouping) {
        return parenthesize("group", grouping.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal literal) {
        if (literal == null) return "nil";
        return literal.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary unary) {
        return parenthesize(unary.operator.lexeme, unary.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable variable) {
        // placeholder so that class can compile
        return null;
    }

    @Override
    public String visitAssignExpr(Expr.Assign assign) {
        // placeholder so that class can compile
        return null;
    }

    @Override
    public String visitLogicalExpr(Expr.Logical logical) {
        // placeholder so that class can compile
        return null;
    }

    private String parenthesize(String name, Expr... expressions) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);

        for (Expr expr : expressions) {
            builder.append(" ");
            // recursively print sub-expressions
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
