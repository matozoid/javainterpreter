package com.laamella.javainterpreter.scope;

public abstract class Scoped {
    private final String name;

    protected Scoped(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
