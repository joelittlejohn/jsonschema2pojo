package com.mysema.examples;

class TryMe {
    
     void someMethod(int num) {}

     void useIt() {
        int n = 5;
        someMethod(Integer.toString(n + 1).length() * 5);
    }
     
}