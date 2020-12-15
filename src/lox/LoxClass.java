package lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    public static final String INIT_KEYWORD = "init";
    final String name;
    final LoxClass superclass;
    Map<String, LoxFunction> methods = new HashMap<>();

    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    LoxFunction findMethod(String name) {
        return methods.get(name);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction constructor = findMethod(INIT_KEYWORD);

        if (constructor != null) {
            constructor.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public int arity() {
        LoxFunction constructor = findMethod(INIT_KEYWORD);
        if (constructor == null) {
            return 0;
        }
        return constructor.arity();
    }

    @Override
    public String toString() {
        return name;
    }
}
