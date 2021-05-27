package com.laamella.javainterpreter;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.printer.YamlPrinter;
import com.laamella.javainterpreter.result.*;
import com.laamella.javainterpreter.scope.Scope;
import com.laamella.javainterpreter.scope.Scoped;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class JavaInterpreter {

    private final JavaParser javaParser;
    private final InterpreterVisitor interpreterVisitor;
    private final Scope globalScope = new Scope(null);

    public static void main(String[] args) {
        Object result = new JavaInterpreter().interpret(args[0]);
        System.out.println(result.toString());
    }

    public JavaInterpreter() {
        javaParser = new JavaParser();
        interpreterVisitor = new InterpreterVisitor();
    }

    public Result interpret(String input) {
        ParseResult<Expression> expressionParseResult = javaParser.parseExpression(input);
        if (expressionParseResult.isSuccessful()) {
            return expressionParseResult.getResult().get().accept(interpreterVisitor, globalScope);
        }
        ParseResult<Statement> statementParseResult = javaParser.parseStatement(input);
        if (statementParseResult.isSuccessful()) {
            dump(statementParseResult);
            return statementParseResult.getResult().get().accept(interpreterVisitor, globalScope);
        }
        ParseResult<BodyDeclaration<?>> bodyDeclarationParseResult = javaParser.parseBodyDeclaration(input);
        if (bodyDeclarationParseResult.isSuccessful()) {
            return bodyDeclarationParseResult.getResult().get().accept(interpreterVisitor, globalScope);
        }
        throw new RuntimeException("Don't understand!");
    }

    private void dump(ParseResult<? extends Node> statementParseResult) {
        Node statement = statementParseResult.getResult().get();
        System.out.println(new YamlPrinter(true).output(statement));
    }

    private class InterpreterVisitor extends GenericVisitorAdapter<Result, Scope> {
        @Override
        public Result visit(IntegerLiteralExpr n, Scope scope) {
            return new ValueResult(n.asNumber().intValue());
        }

        @Override
        public Result visit(StringLiteralExpr n, Scope scope) {
            return new ValueResult(n.asString());
        }

        @Override
        public Result visit(BinaryExpr n, Scope scope) {
            Object left = n.getLeft().accept(this, scope).getValue();
            Result accept = n.getRight().accept(this, scope);
            Object right = accept.getValue();
            if (n.getOperator() == BinaryExpr.Operator.PLUS && (left instanceof String || right instanceof String)) {
                return new ValueResult(left.toString() + right.toString());
            }

            return new ValueResult(switch (n.getOperator()) {
                case OR -> null;
                case AND -> null;
                case BINARY_OR -> null;
                case BINARY_AND -> null;
                case XOR -> null;
                case EQUALS -> null;
                case NOT_EQUALS -> null;
                case LESS -> null;
                case GREATER -> (int) left > (int) right;
                case LESS_EQUALS -> null;
                case GREATER_EQUALS -> null;
                case LEFT_SHIFT -> null;
                case SIGNED_RIGHT_SHIFT -> null;
                case UNSIGNED_RIGHT_SHIFT -> null;
                case PLUS -> (int) left + (int) right;
                case MINUS -> null;
                case MULTIPLY -> (int) left * (int) right;
                case DIVIDE -> null;
                case REMAINDER -> null;
            });
        }

        @Override
        public Result visit(VariableDeclarationExpr n, Scope scope) {
            for (VariableDeclarator variable : n.getVariables()) {
                Class<?> resolvedType = resolveType(variable.getType());
                String name = variable.getName().asString();
                Object initializer = variable.getInitializer().map(init -> init.accept(this, scope).getValue()).orElseGet(() -> defaultForType(variable.getType()));
                scope.add(new Var(name, resolvedType, initializer));
            }
            return null;
        }

        @Override
        public Result visit(NameExpr n, Scope scope) {
            Scoped resolved = scope.resolve(n.getNameAsString());
            if (resolved instanceof Var) {
                return new VariableResult((Var) resolved);
            }
            return new NothingResult();
        }

        @Override
        public Result visit(MethodDeclaration n, Scope scope) {
            // No body means an interface declaration. We don't do that.
            n.getBody().ifPresent(body -> {
                String name = n.getNameAsString();
                Type type = n.getType();
                List<Param> parameters = n.getParameters().stream()
                        .map(param -> new Param(param.getNameAsString(), resolveType(param.getType())))
                        .collect(toList());
                scope.add(new Meth(name, body, resolveType(type), parameters));
            });
            return null;
        }

        @Override
        public Result visit(MethodCallExpr n, Scope scope) {
            String name = n.getNameAsString();
            List<Expression> arguments = n.getArguments();
            Meth method = (Meth) scope.resolve(name);
            List<Param> parameters = method.parameters;
            if (parameters.size() != arguments.size()) {
                throw new IllegalArgumentException("paramcount/argcount mismatch");
            }
            LinkedHashMap<String, Object> args = new LinkedHashMap<>();
            for (int i = 0; i < parameters.size(); i++) {
                String paramName = parameters.get(i).name;
                Object arg = arguments.get(i).accept(this, scope).getValue();
                args.put(paramName, arg);
            }
            Result result = method.call(args, this, scope);
            if (result instanceof ReturningResult) {
                return new ValueResult(((ReturningResult) result).result);
            }
            return result;
        }

        @Override
        public Result visit(BlockStmt n, Scope scope) {
            List<Statement> statements = n.getStatements();
            int line = 0;
            while (line < statements.size()) {
                Result result = statements.get(line).accept(this, scope);
                if (result instanceof ReturningResult) {
                    return result;
                }
                line++;
            }
            return null;
        }

        @Override
        public Result visit(ReturnStmt n, Scope arg) {
            Object result = n.getExpression().map(e -> e.accept(this, arg).getValue()).orElse(null);
            return new ReturningResult(result);
        }

        @Override
        public Result visit(WhileStmt n, Scope scope) {
            Statement body = n.getBody();
            Expression condition = n.getCondition();
            while (condition.accept(this, scope).isTrue()) {
                Result result = body.accept(this, new Scope(scope));
                if (result instanceof ReturningResult) {
                    return new NothingResult();
                }
            }
            return null;
        }

        @Override
        public Result visit(UnaryExpr n, Scope scope) {
            Result var = n.getExpression().accept(this, scope);
            switch (n.getOperator()) {
                case POSTFIX_DECREMENT -> var.setValue((int) var.getValue() - 1);
                case POSTFIX_INCREMENT -> var.setValue((int) var.getValue() + 1);
            }
            return var;
        }
    }

    private Class<?> resolveType(Type type) {
        if (type.isPrimitiveType()) {
            return type.toPrimitiveType().flatMap(pt -> Optional.of(getClassForPrimitiveType(pt.getType()))).get();
        }

        String className = type.asString();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
        }
        try {
            return Class.forName("java.lang." + className);
        } catch (ClassNotFoundException e) {
        }
        throw new RuntimeException("Class not found " + className);
    }

    private Class<?> getClassForPrimitiveType(PrimitiveType.Primitive type) {
        return switch (type) {
            case BOOLEAN -> Boolean.class;
            case CHAR -> Character.class;
            case BYTE -> Byte.class;
            case SHORT -> Short.class;
            case INT -> Integer.class;
            case LONG -> Long.class;
            case FLOAT -> Float.class;
            case DOUBLE -> Double.class;
        };
    }

    private Object getInitializerForPrimitiveType(PrimitiveType.Primitive type) {
        return switch (type) {
            case BOOLEAN -> false;
            case CHAR -> '\u0000';
            case BYTE -> (byte) 0;
            case SHORT -> (short) 0;
            case INT -> 0;
            case LONG -> 0L;
            case FLOAT -> 0F;
            case DOUBLE -> 0D;
        };
    }

    private Object defaultForType(Type type) {
        return type.toPrimitiveType().map(pt -> getInitializerForPrimitiveType(pt.getType())).get();
    }
}
