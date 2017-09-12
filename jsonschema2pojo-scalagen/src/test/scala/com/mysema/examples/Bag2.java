
package com.mysema.examples;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Bag2 {
    
    private HashMap<String, Type> nameTable;
    
    private ArrayList<ArrayList<Type>> itemTable;
    
    private int capacity;
    
    private int mass;
    
    private int levelIndex;
    
    private int currentLevel;
    
    private int currentCounter;
    
    private boolean showing;
    
    protected abstract int capacity();

    protected abstract int forgetRate();


}
