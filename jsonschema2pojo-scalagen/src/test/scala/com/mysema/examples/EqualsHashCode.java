package com.mysema.examples;

public class EqualsHashCode {
    
    // NOTE: override should be added implicitly
    
    public boolean equals(Object o) {
        return false;
    }
    
    public int hashCode() {
        return super.hashCode();
    }
    
    public String toString() {
        return "";
    }

}
