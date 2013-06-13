
package com.googlecode.jsonschema2pojo.integration.generation.gsonit;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;


/**
 * A geographical coordinate
 * 
 */
@Generated("com.googlecode.jsonschema2pojo")
public class WarehouseLocation {

    private Double latitude;
    private Double longitude;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
