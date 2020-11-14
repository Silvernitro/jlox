package lox;

public class Interpreter implements Expr.Visitor<Object> {

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

    public void interpret(Expr expr) {
        try {
            Object result = evaluate(expr);
            System.out.println(stringify(result));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
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
