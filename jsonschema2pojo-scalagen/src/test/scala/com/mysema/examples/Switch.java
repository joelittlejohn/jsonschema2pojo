package com.mysema.examples;

public class Switch {

    public static void method1() {
        switch (42) {
            default: System.out.println(42);
        }
        System.out.println("Hello World!");
    }
    
    public static void method2() {        
        System.out.println("Hello World!");
        switch (42) {
            default: System.out.println(42);
        }
    }
    
    public static void method3() {
        switch (42) {
            default: System.out.println(42);
        }
    }
    
    public static void method4() {
        System.out.println("Hello World!");
    }
    
}
