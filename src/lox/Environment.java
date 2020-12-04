package lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token token) {
        String name = token.lexeme;
        if (values.containsKey(name)) {
            return values.get(name);
        }
        throw new RuntimeError(token, "Undefined variable '" + name + "'.");
    }

    void assign(Token name, Object value) {
        if (!values.containsKey(name.lexeme)) {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
        values.put(name.lexeme, value);
    }
}
