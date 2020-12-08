package lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;

    LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // TODO: check why we extend GLOBALS and not curr environment
        Environment local = new Environment(interpreter.GLOBALS);

        // Note: args and params are guaranteed to be same size due to arity check
        for (int i = 0; i < arguments.size(); i++) {
            String paramName = declaration.params.get(i).lexeme;
            Object argument = arguments.get(i);
            // bind arg to param
            local.define(paramName, argument);
        }

        interpreter.executeBlock(declaration.body, local);

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
