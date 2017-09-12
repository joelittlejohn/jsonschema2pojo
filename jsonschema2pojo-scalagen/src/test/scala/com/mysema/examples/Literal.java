package com.mysema.examples;


public class Literal {
    
    public void doSomething() {
        final String s = query("");
        System.out.println(s);
    }
    
    public void doSomething2() {
        System.out.println(query(""));
    }

    private String query(String string) {
        return null;
    }

}
