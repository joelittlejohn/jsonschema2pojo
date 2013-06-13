
package com.googlecode.jsonschema2pojo.integration.generation.gsonit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;


/**
 * Product
 * <p>
 * 
 * 
 */
@Generated("com.googlecode.jsonschema2pojo")
public class ProductSet {

    /**
     * The unique identifier for a product
     * 
     */
    private Double id;
    private String name;
    private Double price;
    private Set<String> tags = new HashSet<String>();
    /**
     * 
     */
    private Dimensions dimensions;
    /**
     * A geographical coordinate
     * 
     */
    @SerializedName("warehouse_location")
    private WarehouseLocation warehouseLocation;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The unique identifier for a product
     * 
     */
    public Double getId() {
        return id;
    }

    /**
     * The unique identifier for a product
     * 
     */
    public void setId(Double id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * 
     */
    public Dimensions getDimensions() {
        return dimensions;
    }

    /**
     * 
     */
    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * A geographical coordinate
     * 
     */
    public WarehouseLocation getWarehouseLocation() {
        return warehouseLocation;
    }

    /**
     * A geographical coordinate
     * 
     */
    public void setWarehouseLocation(WarehouseLocation warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
