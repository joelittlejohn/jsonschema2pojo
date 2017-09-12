package com.mysema.examples;

public class SwitchCase {

    public void run() {
        int i = hashCode();
        switch (i) {
        case 0: System.out.println(0);
        case 1: System.out.println(1);
        default: System.out.println(i);
        }
    }
    
    public void run2() {
        int i = hashCode();
        switch (i) {
        case 0: 
        case 1: System.out.println(1);
        default: System.out.println(i);
        }
    }
    
    public void run3() {
        int i = hashCode();
        switch (i) {
        case 0: break;
        case 1: System.out.println(1); break;
        default: System.out.println(i);
        }
    }
    
    public void run4() {
        String str = null;
        int i = hashCode();
        switch (i) {
        case 0: str = "0"; break;
        case 1: str = "1"; break;
        default: str = String.valueOf(i);
        }
        System.err.println(str);
    }
    
}
