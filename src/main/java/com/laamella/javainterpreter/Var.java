package com.laamella.javainterpreter;

import com.laamella.javainterpreter.scope.Scoped;

public class Var extends Scoped {
    public final Class<?> type;
    public Object value;

    public Var(String name, Class<?> type, Object value) {
        super(name);
        this.type = type;
        this.value = value;
    }
}
