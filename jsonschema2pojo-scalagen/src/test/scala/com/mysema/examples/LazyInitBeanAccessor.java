package com.mysema.examples;

public class LazyInitBeanAccessor {

   private String value;
   
   public String getValue() {
       if (value == null) {
           value = "XXX";
       }
       return value;
   }
        
}
