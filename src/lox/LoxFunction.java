package lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    LoxFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment local = new Environment(closure);

        // Note: args and params are guaranteed to be same size due to arity check
        for (int i = 0; i < arguments.size(); i++) {
            String paramName = declaration.params.get(i).lexeme;
            Object argument = arguments.get(i);
            // bind arg to param
            local.define(paramName, argument);
        }

        try {
            interpreter.executeBlock(declaration.body, local);
        } catch (Return r) {
            return r.value;
        }

        // return null for now until return statements are handled
        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
