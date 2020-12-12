package lox;

import java.util.List;

public class LoxClass implements LoxCallable {
    final String name;

    LoxClass(String name) {
        this.name = name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return new LoxInstance(this);
    }

    @Override
    public int arity() {
        // TODO: support multi-arg constructors
        return 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
