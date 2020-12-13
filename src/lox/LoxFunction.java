package lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure) {
        this(declaration, closure, false);
    }

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
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
            if (!isInitializer) return r.value;
            // if fxn is an initializer, fall-through and return the instance below
        }

        if (isInitializer) return closure.getAt(0, LoxClass.INIT_KEYWORD);
        // default return value if no return stmt is found
        return null;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, isInitializer);
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
