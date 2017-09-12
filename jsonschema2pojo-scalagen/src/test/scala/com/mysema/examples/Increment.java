package com.mysema.examples;

public class Increment {
    
    public void doSomething() {
        int i = 0;
        int[] ints = new int[10];
        while (i < 10) {
            System.out.println(ints[i++]);
        }
    }

}
