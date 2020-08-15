package lox;

import java.util.ArrayList;

public class Scanner {
    private final String source;
    private ArrayList<Token> tokens = new ArrayList<>();

    // position of the current lexeme's start
    private int start = 0;
    // position of char we are curr at
    private int current = 0;
    // the line that the curr lexeme is on
    private int currentLine = 1;

    // Scanner takes in any string to scan
    Scanner(String source) {
        this.source = source;
    }

    ArrayList<Token> scanTokens() {
        while (!isEnd()) {
            // set start of token to the current position
            start = current;
            // scan this lexeme
            scanToken();
        }

        // add EOF token when we reach the end
        tokens.add(new Token(TokenType.EOF, "", null, this.currentLine));
        return tokens;
    }

    private boolean isEnd() {
        return current > this.source.length();
    }

    private void scanToken() {
        // start by advancing scanner cursor
        current++;
        // read previous char
        char c = this.source.charAt(current - 1);

        switch (c) {
            //---------- single-character tokens ---------//
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;

            //---------- two-character tokens (operators) ---------//
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL :
                                 TokenType.GREATER);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL :
                                 TokenType.LESS);
                break;
            case '/':
                if (match('/')) {
                    // if this is a comment, consume the rest of the line
                    while (!isEnd() && peek() != '\n') {
                        this.current++;
                    }
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            //---------- whitespaces ---------//
            case '\r':
            case '\t':
                break;
            case '\n':
                this.currentLine++;
                break;

            //---------- literals ---------//
            case '"': string(); break;

            default:
                Lox.error(this.currentLine, "Unexpected token: " + c);
                break;
        }
    }

    private boolean match(char c) {
        if (isEnd() || this.source.charAt(this.current) != c) return false;
        // only consume this char if match
        this.current++;
        return true;
    }

    // peeks the current character without consuming it
    private char peek() {
        if (isEnd()) return '\0';
        return this.source.charAt(this.current);
    }

    private void string() {
        while (!isEnd() && peek() != '"') {
            // allow multiline strings
            if (peek() == '\n') this.currentLine++;
            this.current++;
        }
        // if we are at the end at this point, then the string was not closed
        if (isEnd()) {
            Lox.error(this.currentLine, "unterminated string");
            return;
        }

        // get the string value without quotes
        String value = source.substring(this.start + 1, this.current);
        addToken(TokenType.STRING, value);

        // consume the closing quote
        this.current++;
    }

    private void number() {
        while (!isEnd() && isDigit(peek())) {
            this.current++;
        }

        // handle floating point digits
        if (peek() == '.' && isDigit(peekNext())) {
            // skip the '.'
            this.current++;
            // continue consuming digits
            while(!isEnd() && isDigit(peek())) {
                this.current++;
            }
        }

        double value = Double.parseDouble(source.substring(this.start,
                                                   this.current + 1));
        addToken(TokenType.NUMBER, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // peeks 1 character ahead without consuming it
    private char peekNext() {
        if (this.current + 1 > this.source.length()) return '\0';
        return this.source.charAt(this.current + 1);
    }

    // add non-literal token
    private void addToken(TokenType token) {
        String lexeme = this.source.substring(start, current);
        tokens.add(new Token(token, lexeme, null, this.currentLine));
    }

    // add literal token (overloaded)
    private void addToken(TokenType token, Object literal) {
        String lexeme = this.source.substring(start, current);
        tokens.add(new Token(token, lexeme, literal, this.currentLine));
    }
}
