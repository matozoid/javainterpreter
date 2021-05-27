package com.laamella.javainterpreter.scope;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Map<String, Scoped> scopedThings = new HashMap<>();
    private final Scope parentScope;

    public Scope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    public void add(Scoped scoped) {
        scopedThings.put(scoped.getName(), scoped);
    }

    public Scoped resolve(String name) {
        Scoped scoped = scopedThings.get(name);
        if (scoped == null) {
            if (parentScope == null) {
                return null;
            }
            return parentScope.resolve(name);
        }
        return scoped;
    }
}
