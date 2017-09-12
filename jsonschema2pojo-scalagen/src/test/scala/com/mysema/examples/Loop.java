package com.mysema.examples;

public class Loop {

    boolean condition_and_early_return_dont_contain_b(String str) {
        for (String b : java.util.Arrays.asList("a", "b")) {
            if (str.startsWith("a")) {
                return true;
            }
        }
        return false;
    }
    
    boolean condition_and_early_return_contain_b_once(String str) {
        for (String b : java.util.Arrays.asList("a", "b")) {
            if (str.startsWith(b)) {
                return b.length() > 0;
            }
        }
        return false;
    }
    
    boolean condition_and_early_return_contain_b_multiple_times(String str) {
        for (String b : java.util.Arrays.asList("a", "b")) {
            if (str.startsWith(b) && b.length() < 10) {
                return b.length() > 0 || b.length() < 15;
            }
        }
        return false;
    }

}
