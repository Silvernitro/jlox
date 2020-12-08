package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
    public static final List<String> AST_TYPES = Arrays.asList(
        "Binary: Expr left, Token operator, Expr right",
        "Call: Expr callee, List<Expr> arguments, Token paren",
        "Grouping: Expr expression",
        "Literal: Object value",
        "Unary: Token operator, Expr right",
        "Variable: Token name",
        "Assign: Token name, Expr value",
        "Logical: Expr left, Token operator, Expr right"
    );

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("No filepath specified for generateAST.");
            System.exit(64);
        }
        String outputDirectory = args[0];
        defineAST(outputDirectory, "Expr", AST_TYPES);
        defineAST(outputDirectory, "Stmt", Arrays.asList(
            "Expression: Expr expression",
            "Print: Expr expression",
            "Var: Token name, Expr initializer",
            "Block: List<Stmt> statements",
            "If: Expr condition, Stmt thenBranch, Stmt elseBranch",
            "While: Expr condition, Stmt body"
        ));
    }

    private static void defineAST(String outputDirectory, String baseName, List<String> types)
            throws IOException {
        String outputFilePath = outputDirectory + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(outputFilePath, StandardCharsets.UTF_8);

        writer.println("package lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        writer.println("    abstract <R> R accept(Visitor<R> visitor);");
        writer.println();

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }


    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" + typeName + " " + typeName.toLowerCase() +
                                   ");");
        }
        writer.println("    }\n");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) {
        writer.println("    static class " + className + " extends " + baseName + " {");

        // declare fields
        String[] fieldArray = fields.split(", ");
        for (String field : fieldArray) {
            writer.println("        final " + field.trim() + ";");
        }
        writer.println();

        // write the constructor
        writer.println("        " + className + "(" + fields + ") {");
        for (String field : fieldArray) {
            String name = field.split(" ")[1].trim();
            writer.println("            this." + name + " = " + name + ";");
        }
        writer.println("        }\n");

        // write the visitor pattern's accept method
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");
        writer.println();
    }
}
