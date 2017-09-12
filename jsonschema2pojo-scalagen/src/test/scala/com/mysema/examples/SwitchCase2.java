package com.mysema.examples;

public class SwitchCase2 {
    
    public void doSmth(int i) {
        switch (i) {
        case 3:
            return;
        }
    }
    
    public String doSmthElse(int i) {
        switch (i) {
        case 3:
            return "abc";
        default:
            return null;
        }
    }

}
