package com.mysema.examples;

import java.util.ArrayList;
import java.util.List;

public class IfElse {
    
    public void ifElse() {
        String property = "x";
        if (System.currentTimeMillis() > 0) {
            property = "y";
        } else {
            property = "z";
        }
        System.out.println(property);
    }
    
    public void ifElse2() {
        boolean success;
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        if (list.size() == 2) {
            success = true;
        } else {
            success = false;
        }
    }

}
