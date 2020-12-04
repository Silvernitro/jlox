package lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    /** The global environment has no parent **/
    Environment() {
        enclosing = null;
    }

    /** All non-global environments must have a parent **/
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token token) {
        String name = token.lexeme;
        if (values.containsKey(name)) {
            return values.get(name);
        }

        if (enclosing != null) {
            return enclosing.get(token);
        }

        throw new RuntimeError(token, "Undefined variable '" + name + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
