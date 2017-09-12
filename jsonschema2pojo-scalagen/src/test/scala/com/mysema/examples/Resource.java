/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.examples;



/**
 * Resource provides
 * 
 * @author tiwe
 * @version $Id$
 */
public class Resource {
    
    private final boolean forward;

    private final boolean l10n;

    private String path;
    
    public Resource(String path, boolean forward, boolean l10n) {
        this.path = path;
        this.forward = forward;
        this.l10n = l10n;
    }

    public boolean equals(Object o) {
        return o instanceof Resource && ((Resource) o).path.equals(path);
    }
    
    public int hashCode(){
        return path.hashCode();
    }

    public String getPath() {
        return path;
    }

    public void addPathPrefix(String basePath) {
        this.path = basePath + path;
    }

    public boolean isForward() {
        return forward;
    }

    public boolean isL10n() {
        return l10n;
    }
    
    @Override
    public String toString(){
        return path;
    }

}