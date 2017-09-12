package com.mysema.examples;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Diamond {
    Set<String> set1 = new HashSet<>();
    void infer() {
    	Set<String> set2 = new HashSet<>();
    	Map<String, Integer> map = new HashMap<>();
    }
}
