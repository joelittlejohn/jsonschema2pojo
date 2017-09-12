package com.mysema.examples;

public class Bean4 {
    
    public static class T1 {}
    
    public static class C {
        private T1 prop;

        C(T1 prop) {
            setProp(prop);
        }

        public void setProp(T1 prop) {
            this.prop = prop;
        }

        public T1 getProp() {
            return prop;
        }
    }
    
    public static class D {
        private T1 prop;

        D(T1 prop) {
            this.prop = prop;
        }

        public void setProp(T1 prop) {
            this.prop = prop;
        }

        public T1 getProp() {
            return prop;
        }
    }

}
