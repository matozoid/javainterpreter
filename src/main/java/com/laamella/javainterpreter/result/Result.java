package com.laamella.javainterpreter.result;

public class Result {
    public boolean isTrue() {
        throw new RuntimeException("Not a boolean!");
    }

    public Object getValue() {
        throw new RuntimeException("No value!");
    }

    public void setValue(Object newValue) {
        throw new RuntimeException("Not mutable!");
    }
}
