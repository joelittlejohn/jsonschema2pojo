
package org.jsonschema2pojo.integration.generation.gsonit;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;


/**
 * Example Schema
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class Person {

    private String firstName;
    private String lastName;
    /**
     * Age in years
     * 
     */
    private Integer age;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Age in years
     * 
     */
    public Integer getAge() {
        return age;
    }

    /**
     * Age in years
     * 
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
