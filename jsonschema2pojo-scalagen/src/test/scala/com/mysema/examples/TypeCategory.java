/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.examples;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * TypeCategory defines the expression type used for a Field
 *
 * @author tiwe
 *
 */
public enum TypeCategory {
    /**
     *
     */
    SIMPLE(null),
    /**
     *
     */
    MAP(null),
    /**
     *
     */
    COLLECTION(null),
    /**
     *
     */
    LIST(COLLECTION),
    /**
     *
     */
    SET(COLLECTION),
    /**
     *
     */
    ARRAY(null),
    /**
     *
     */
    COMPARABLE(SIMPLE),
    /**
     *
     */
    BOOLEAN(COMPARABLE, Boolean.class.getName()),
    /**
     *
     */
    DATE(COMPARABLE, java.sql.Date.class.getName(), "org.joda.time.LocalDate"),
    /**
     *
     */
    DATETIME(COMPARABLE,
        java.util.Calendar.class.getName(),
        java.util.Date.class.getName(),
        java.sql.Timestamp.class.getName(),
        "org.joda.time.LocalDateTime",
        "org.joda.time.Instant",
        "org.joda.time.DateTime",
        "org.joda.time.DateMidnight"),
    /**
     * 
     */
    ENUM(COMPARABLE),
    /**
     * 
     */
    CUSTOM(null),    
        
    /**
     *
     */
    ENTITY(null),

    /**
     *
     */
    NUMERIC(COMPARABLE),
    /**
     *
     */
    STRING(COMPARABLE, String.class.getName()),
    /**
     *
     */
    TIME(COMPARABLE, java.sql.Time.class.getName(), "org.joda.time.LocalTime");

    @Nullable
    private final TypeCategory superType;

    private final Set<String> types;

    TypeCategory(@Nullable TypeCategory superType, String... t){
        this.superType = superType;
        this.types = new HashSet<>(t.length);
        for (String type : t){
            this.types.add(type);
        }
    }

    public TypeCategory getSuperType() {
        return superType;
    }

    public boolean supports(Class<?> cl){
        return supports(cl.getName());
    }

    public boolean supports(String className){
        return types.contains(className);
    }

    /**
     * transitive and reflexive subCategoryOf check
     *
     * @param ancestor
     * @return
     */
    public boolean isSubCategoryOf(TypeCategory ancestor){
        if (this == ancestor){
            return true;
        }else if (superType == null){
            return false;
        }else{
            return superType == ancestor || superType.isSubCategoryOf(ancestor);
        }
    }

    public static TypeCategory get(String className){
        for (TypeCategory category : values()){
            if (category.supports(className)){
                return category;
            }
        }
        return SIMPLE;
    }

}
