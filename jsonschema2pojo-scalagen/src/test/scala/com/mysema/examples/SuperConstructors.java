package com.mysema.examples;

public class SuperConstructors extends SuperConstructorsSuperClass {
    
    public SuperConstructors() {
        this("first", "last");
    }
    
    public SuperConstructors(String first, String last) {
        super(first);
    }

}

class SuperConstructorsSuperClass {
    
    public SuperConstructorsSuperClass(String first) {
    }
}
