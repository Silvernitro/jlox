# jlox

Based off the book, Crafting Interpreters by Bob Nystrom, jlox is an interpreter for the Lox language. 

### About Lox (the language)
Lox is a programming language used in the book for the main purpose of introducing compilers and interpreters to novices.

Lox is a fully-fledged programming language in the OOP vein of languages. Lox is dynamically typed (think JavaScript and Python), not statically typed (think Java).
Being an OOP language, Lox supports classes, which can contain state and behaviour in the form of fields and methods respectively.
Lox classes can also support constructors/initializers. Besides this, Lox supports all other typical language constructs, including logical operators, if-else blocks,
while and for loops, and first-class functions.

Without dumping the entire documentation of Lox here, the complete syntax and lexical grammar of the Lox language written in Backus-Naur form (BNF)
can be found [here](https://craftinginterpreters.com/appendix-i.html).

### About jlox (the interpreter)
jlox is a tree-walk interpreter for the Lox language written in Java. More strictly, jlox is a transpiler that transpiles Lox source code into Java runtime
representations that run on the JVM. You can either pass jlox a Lox script file to run or fire up the REPL. 

As with any language, jlox contains a source code scanner to scan source code strings into lexemes and finally, tokens.

jlox then uses a recursive descent parser to parse the tokens into an abstract syntax tree (AST) of statement and expression nodes. The parser supports parse errors
and synchronization when a parse error is detected.

Interestingly (to me at least), jlox performs a single static resolution pass after parsing to resolve all variables before execution of the code begins. On top of this,
jlox's resolver also detects and reports a couple of static resolution errors (more to come!)

Last but not least, jlox uses a tree-walk interpreter to traverse the AST and execute everything on the JVM. Naturally, things that mess up here get reported as 
runtime errors.

### What's next?
Following the book's progression, jlox is but one half of the Lox journey. jlox is **painfully** slow. As such, the next step is *clox*, a bytecode compiler written 
in C. I'll also go along with the book and write a VM to execute the bytecode on.

