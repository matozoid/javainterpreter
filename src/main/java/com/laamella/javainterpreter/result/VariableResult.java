package com.laamella.javainterpreter.result;

import com.laamella.javainterpreter.Var;

public class VariableResult extends Result {
    public final Var var;

    public VariableResult(Var var) {
        this.var = var;
    }

    @Override
    public Object getValue() {
        return var.value;
    }

    @Override
    public void setValue(Object newValue) {
        var.value = newValue;
    }
}
