package com.mysema.examples;

public class SuperConstructors extends SuperClass {
    
    public SuperConstructors() {
        this("first", "last");
    }
    
    public SuperConstructors(String first, String last) {
        super(first);
    }

}

class SuperClass {
    
    public SuperClass(String first) {
    }
}
