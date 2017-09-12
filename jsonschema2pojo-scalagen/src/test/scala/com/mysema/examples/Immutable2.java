package com.mysema.examples;

public class Immutable2 {
    
    private final String firstName;
    
    private final String lastName;
    
    public Immutable2(String f, String l) {
        firstName = f;
        lastName = l;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    
}
