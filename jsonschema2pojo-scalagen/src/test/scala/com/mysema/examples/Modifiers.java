package com.mysema.examples;

public class Modifiers {

    private transient String foo = "foo";
    
    private volatile String bar = "bar";
    
    public synchronized void foobar() {
        System.out.println("Hello World");
    }
    
}
