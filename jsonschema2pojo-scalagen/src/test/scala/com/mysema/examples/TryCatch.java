package com.mysema.examples;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class TryCatch {
    
    public void run() {
        try {
            for (String str : Arrays.asList("a","b","c")) {
                System.out.println(str);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getClass().getName());
            System.err.println(e.getMessage());
        } finally {
            System.out.println("done");
        }
    }

    void foo() {
        try {

        } finally {

        }
    }

    void bar() {
        try {
        } catch (Exception e) {
        }
    }

    void baz() {
        try {
            bar();
        } catch (Exception e) {
        } finally {
            foo();
        }
    }

    void buzz() {
        try {

        } finally {
            baz();
        }
    }
    
    void simplified() {
        try {
            System.getProperty("xxx").substring(1);
        } catch (NullPointerException n) {
            throw new RuntimeException(n);
        }
    }
    
    void simplified2() {
        try {
            System.getProperty("xxx").substring(1);
        } catch (IllegalArgumentException | NullPointerException n) {
            throw new RuntimeException(n);
        }
    }
    
    void multiCatch() {
    	try {
    		System.out.println("here");
    	} catch (Error | Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    void tryWithResources() throws IOException {
    	try (InputStream in1 = makeInput();
    	     InputStream in2 = makeInput()) {
    		System.out.println(in1 + ", " + in2);
    	} catch (IOException e) {
    		
    	} finally {
    		System.out.println("done");
    	}
    	
    	try (InputStream in = new ByteArrayInputStream(new byte[0])) {
    		System.out.println(in);
    	}
    }
    
    FileInputStream makeInput() throws IOException {
    	return new FileInputStream("abc.txt");
    }

}
