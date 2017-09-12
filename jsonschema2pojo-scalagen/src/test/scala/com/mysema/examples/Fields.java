package com.mysema.examples;

import java.util.HashMap;
import java.util.Map;

public class Fields {
    
    private Map<String,String> properties = new HashMap<String,String>();
    
    public Fields() {
    }
    
    public Fields(Map<String,String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
    
    

}
