package com.laamella.javainterpreter.result;

public class ValueResult extends Result {
    public final Object value;

    public ValueResult(Object value) {
        this.value = value;
    }

    @Override
    public boolean isTrue() {
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return super.isTrue();
    }

    @Override
    public Object getValue() {
        return value;
    }
}
