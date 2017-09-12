package com.mysema.examples;

public abstract class Person {

    private int age = 0;
 
    public abstract String firstName();
 
    public final String lastName() {
        return "Spiewak";
    }
 
    public synchronized void incrementAge() {
        age += 1;
    }
 
    //public native String hardDriveAge();
    
}