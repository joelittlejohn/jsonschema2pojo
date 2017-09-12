package com.mysema.examples;

import java.util.List;

public class Bean3 extends Superclass{

    private List<String> names;

    public Bean3(List<String> n) {
        super(n.toString());
        this.names = n;
    }
    
    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

}

    
class Superclass {
    
    public Superclass(String s) {
        // TODO Auto-generated constructor stub
    }
}
