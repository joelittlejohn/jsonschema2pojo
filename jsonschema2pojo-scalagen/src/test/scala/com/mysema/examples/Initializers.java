package com.mysema.examples;

public class Initializers {
    
    static final String staticValue;
    
    static {
        staticValue = "xx";
    }
    
    final String value;
    
    {
        value = "x";
    }

}
