/*
 * Copyright 2011, Mysema Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mysema.examples;

import javax.annotation.Nullable;

import com.mysema.codegen.model.TypeCategory;

/**
 * PropertyType defines the Path type to be used for a Domain property
 *
 * @author tiwe
 *
 */
public enum PropertyType {
    /**
     * 
     */    
    COMPARABLE(TypeCategory.COMPARABLE),
    
    /**
     * 
     */    
    ENUM(TypeCategory.ENUM),
    
    /**
     * 
     */    
    DATE(TypeCategory.DATE),
    
    /**
     * 
     */    
    DATETIME(TypeCategory.DATETIME),
    
    /**
     * 
     */    
    NONE(null),
    
    /**
     * 
     */
    NUMERIC(TypeCategory.NUMERIC),
    
    /**
     * 
     */    
    SIMPLE(TypeCategory.SIMPLE),
    
    /**
     * 
     */
    STRING(TypeCategory.STRING),
    
    /**
     * 
     */
    TIME(TypeCategory.TIME),
    
    /**
     *
     */
    ENTITY(TypeCategory.ENTITY);
    
    @Nullable
    private final TypeCategory category;
    
    PropertyType(@Nullable TypeCategory category) {
        this.category = category;
    }
    
    @Nullable
    public TypeCategory getCategory() {
        return category;
    }

}
