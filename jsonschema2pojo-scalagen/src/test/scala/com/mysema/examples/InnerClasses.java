package com.mysema.examples;

@SuppressWarnings("unused")
public class InnerClasses {
    
    public static class StaticClass {
        
    }
    
    private static class PrivateStaticClass {
        
    }
    
    private class PrivateClass {
        
    }
    
    public class PublicClass {
        
    }
        
    private static final class LoopContext {
        private String note;
        private String text;
        private String paragraphs;
        private int counter;
        private boolean inBib;
        private boolean inA;
        private String reference;
        private String url;

        private LoopContext() {
            note = null;
            text = null;
            counter = 0;
        }
    }

}
