package com.mysema.examples;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Control {
    
    List<Integer> integers = Arrays.asList(1,2,3,4,5);
    
    int[] ints = {1,2,3,4,5};
    
    public void whileLoop() {
        int i = 0;
        while (i < integers.size()) {
            System.err.println(integers.get(i));
            i++;
        }
    }
    
    public void whileLoop2() {
        Iterator<Integer> i = integers.iterator();
        while (i.hasNext()) {
            System.err.println(i.next());
        }
    }
    
    public void forLoop() {
        for (int i = 0; i < integers.size(); i++) {
            System.err.println(integers.get(i));
        }
    }
    
    public void forLoop2() {
        for (Integer i : integers) {
            System.err.println(i);
        }
    }
    
    public void forLoop3() {
        for (Iterator<Integer> i = integers.iterator(); i.hasNext(); ) {
            System.err.println(i.next());
        }
    }
    
    public void forLoop4() {
        for(int i = 0;; i++){
            if (i > 10) return;
        }
    }
    
    public void forLoopWithIf() {
        for (Integer i : integers) {
            if (i > 0) {
                System.err.println(i);    
            }            
        }
    }
    
    public void forLoopWithIfAndFor() {
        for (Integer i : integers) {
            if (i > 0) {
                for (Integer j : integers) {
                    System.err.println(i + j);    
                }                    
            }            
        }
    }
        
    public void forLoopWithFor() {
        for (Integer i : integers) {
            for (Integer j : integers) {
                System.err.println(i + j);    
            }        
        }
    }  
    
    public int transform(int i) {
        for (int j : ints) {
            if (j == i) {
                return j;
            }
        }
        return -1;
    }
    
    public int transform2(int i) {
        for (int j : ints) {
            if (j == i) {
                return 2 * j;
            }
        }
        return -1;
    }
    
    public void entrySetIterator() {
        Map<String,String> entries = Collections.<String,String>emptyMap();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            System.out.println(entry.getKey()+ " " + entry.getValue());
        }
    }

}
