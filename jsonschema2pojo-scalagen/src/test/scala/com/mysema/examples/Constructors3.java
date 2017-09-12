package com.mysema.examples;


public class Constructors3 {

    class A {
        public A(String str) {
        }
    }

    class B extends A {
        public B() {
            super("some value");
        }

        public B(String str) {
            super(str);
        }
    }

}
