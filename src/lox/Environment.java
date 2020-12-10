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

    Object getAt(int distance, Token name) {
        // Note: Environment.get is not used as it will continue walking up the env chain
        return ancestor(distance).values.get(name.lexeme);
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

    void assignAt(int distance, Token name, Object value) {
        // Note: Environment.assign is not used as it will continue walking up the env chain
        ancestor(distance).values.put(name.lexeme, value);
    }

    // ------------- START OF UTILS -------------- //
    private Environment ancestor(int distance) {
        Environment environment = this;

        for (int i = 0; i < distance; i++) {
            // assert that the resolver has already been run prior to interpreting
            assert environment != null;
            environment = environment.enclosing;
        }

        return environment;
    }
    // ------------- END OF UTILS -------------- //
}
