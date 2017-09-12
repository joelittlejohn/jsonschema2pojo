package com.mysema.examples;

import com.mysema.commons.lang.Assert;

public class Asserts {
    
    private final String firstName, lastName;
    
    public Asserts(String firstName, String lastName) {
        this.firstName = Assert.notNull(firstName, "firstName");
        this.lastName = Assert.notNull(lastName, "lastName");
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    
}
