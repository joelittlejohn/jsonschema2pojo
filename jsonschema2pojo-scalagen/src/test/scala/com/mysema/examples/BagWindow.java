package com.mysema.examples;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class BagWindow extends BagWindowSuper implements ActionListener, AdjustmentListener { 

    private Bag<?> bag;
    
    private String str;
    
    private int showLevel;
    
    public BagWindow(Bag<?> b, String title) {
        super(title);
        bag = b;
        str = "abc";
        adjustmentValueChanged(null);
    }
    
    public int showLevel() {
        return showLevel;
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
    }
    
}

class Bag<T> {
    
}

class BagWindowSuper {
    
    public BagWindowSuper(String title) {
        // do nothing
    }
}