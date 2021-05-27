package com.laamella.javainterpreter;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.laamella.javainterpreter.result.Result;
import com.laamella.javainterpreter.scope.Scope;
import com.laamella.javainterpreter.scope.Scoped;

import java.util.LinkedHashMap;
import java.util.List;

public class Meth extends Scoped implements CallableMethod {
    public final BlockStmt body;
    public final Class<?> aClass;
    public final List<Param> parameters;

    public Meth(String name, BlockStmt body, Class<?> aClass, List<Param> parameters) {
        super(name);
        this.body = body;
        this.aClass = aClass;
        this.parameters = parameters;
    }

    @Override
    public Result call(LinkedHashMap<String, Object> args, GenericVisitorAdapter<Result, Scope> interpreterVisitor, Scope scope) {
        Scope methodScope = new Scope(scope);
        args.forEach((k, v) -> methodScope.add(new Var(k, v.getClass(), v)));
        return body.accept(interpreterVisitor, methodScope);

    }
}
