package com.mysema.examples;

public class Concatenation {
    
    public static String f(String arg1) {
        return arg1;
    }

    private static String f(String arg1, String arg2, String arg3) {
        return arg1;
    }
    
    public static void main(String[] args) {
        String sep = ";";
        String title = "abc".toLowerCase() + ":" + f(sep) + ", fdsa="
                + ", ma " + f(sep) + 
                args[0] + f(args[0], sep, args[1]);
        System.out.println(title);
    }


}
