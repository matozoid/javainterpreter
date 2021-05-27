package com.laamella.javainterpreter;

public class Param {
    public final String name;
    public final Class<?> type;

    public Param(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }
}
