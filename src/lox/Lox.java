package lox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Lox {
    static Interpreter interpreter = new Interpreter();
    // initialize flag to keep track of error handling
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("jlox only accepts 0 or 1 arguments");
            System.exit(64);
        } else if (args.length == 1) {
            // attempt to run the file passed
            runFile(args[0]);
        } else {
            // start REPL
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // check for error flag and exit gracefully
        if (hadError) {
            System.exit(65);
        }
        if (hadRuntimeError) {
            System.exit(70);
        }
    }

    private static void runPrompt() throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader((System.in)));

        while(true) {
            String line = reader.readLine();

            // when ctrl-D is pressed, reader returns null
            if (line == null) {
                break;
            }

            run(line);
        }
    }

    private static void run(String code) {
        hadError = false;

        // STEP 1: SCAN
        Scanner scanner = new Scanner(code);
        ArrayList<Token> tokens = scanner.scanTokens();

        // STEP 2: PARSE
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        // check for parse errors
        if (hadError) return;

        // STEP 3: RESOLVE
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        // check for resolution errors
        if (hadError) return;

        // STEP 4: INTERPRET
        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
       report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.out.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
