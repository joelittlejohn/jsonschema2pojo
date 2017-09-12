package com.mysema.examples;

import java.util.List;

public class Wildcard {
    
    int foo(List<?> list) { 
        return list.size(); 
    }

    int bar(List<? extends CharSequence> list) { 
        return list.size();
    }
}
