package lox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Lox {
    // initialize flag to keep track of error handling
    static boolean hadError = false;

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
    }

    private static void runPrompt() throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader((System.in)));

        while(true) {
            String line = reader.readLine();
            System.out.println(line);

            // when ctrl-D is pressed, reader returns null
            if (line == null) {
                break;
            }

            run(line);
        }
    }

    private static void run(String code) {
        Scanner scanner = new Scanner(code);
        ArrayList<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
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

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
