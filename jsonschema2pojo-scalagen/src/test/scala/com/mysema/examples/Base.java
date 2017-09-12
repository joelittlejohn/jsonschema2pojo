package com.mysema.examples;

class Base {
    
    Base() {
    } 

    Base(String s) {
    } 
}

class Derived extends Base {
    Derived(String s) {
        super(s);
    }
}
