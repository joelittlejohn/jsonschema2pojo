package com.mysema.examples;

public class Immutable {
    
    public static void main(String[] args) {
        Immutable immutable = new Immutable("John", "Doe");
        System.out.println(immutable.getFirstName());
        System.out.println(immutable.getLastName());
    }
    
    private final String firstName;
    
    private final String lastName;

    public Immutable(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    
}
