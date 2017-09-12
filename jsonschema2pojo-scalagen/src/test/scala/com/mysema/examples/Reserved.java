package com.mysema.examples;

public class Reserved {

    String type = "a";
    String var = "b";
    String val = "c";
    String object = "d";
    String def = "d";
    
    public void reservedWords() {
        String type = "a";
        String var = "b";
        String val = "c";
        String object = "d";
        String def = "d";
        System.err.println(type + var + val + object + def);
    }
    
}
