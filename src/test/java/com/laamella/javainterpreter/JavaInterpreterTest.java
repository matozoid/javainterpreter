package com.laamella.javainterpreter;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.laamella.javainterpreter.result.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JavaInterpreterTest {
    @Test
    public void integerLiteral() {
        Result result = new JavaInterpreter().interpret("1");
        assertEquals(1, result.getValue());
    }

    @Test
    public void stringLiteral() {
        Result result = new JavaInterpreter().interpret("\"Hello World!\"");
        assertEquals("Hello World!", result.getValue());
    }

    @Test
    public void addInts() {
        Result result = new JavaInterpreter().interpret("1+1");
        assertEquals(2, result.getValue());
    }

    @Test
    public void addIntAndString() {
        Result result = new JavaInterpreter().interpret("1+\"twee\"");
        assertEquals("1twee", result.getValue());
    }

    @Test
    public void assignAndReadVariable() {
        JavaInterpreter interpreter = new JavaInterpreter();
        Result result = interpreter.interpret("int a=5*5*5;");
        assertNull(result);
        result = interpreter.interpret("a");
        assertEquals(5 * 5 * 5, result.getValue());
    }

    @Test
    public void defineMethod() {
        JavaInterpreter interpreter = new JavaInterpreter();
        // Check that scoping is correct. This "a" must not be used.
        interpreter.interpret("int a=20;");

        interpreter.interpret("int abc(int x, int y) {int a=5; return a+x+y;}");
        Result result = interpreter.interpret("abc(6,7)");
        assertEquals(5 + 6 + 7, result.getValue());
    }

    @Test
    public void whileDo() {
        JavaInterpreter interpreter = new JavaInterpreter();
        interpreter.interpret("int a=20, b=0;");
        interpreter.interpret("while(a>1){a--; b++;}");

        assertEquals(1, interpreter.interpret("a").getValue());
        assertEquals(19, interpreter.interpret("b").getValue());
    }

    @Test
    public void newJdkObject() {
        JavaInterpreter interpreter = new JavaInterpreter();
        interpreter.interpret("java.util.Date d=new java.util.Date(0);");
        Result result = interpreter.interpret("d.toString()");

        assertEquals("faef", result.getValue());
    }
}
