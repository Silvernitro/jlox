package lox;

public class RuntimeError extends RuntimeException {
    Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
