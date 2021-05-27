package com.laamella.javainterpreter;

import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.laamella.javainterpreter.result.Result;
import com.laamella.javainterpreter.scope.Scope;

import java.util.LinkedHashMap;

public interface CallableMethod {
    Result call(LinkedHashMap<String, Object> args, GenericVisitorAdapter<Result, Scope> interpreterVisitor, Scope scope);
}
