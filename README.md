# jsonschema2pojo [![Build Status](https://travis-ci.org/joelittlejohn/jsonschema2pojo.png)](https://travis-ci.org/joelittlejohn/jsonschema2pojo)

_jsonschema2pojo_ generates Java types from JSON Schema (or example JSON) and can annotate those types for data-binding with Jackson 1.x, Jackson 2.x or Gson.

This code is a fork from https://github.com/joelittlejohn/jsonschema2pojo and is based on https://github.com/lsubramanya/jsonschema2pojo

This code improves the property 'deserializationClassProperty' adding to generated pojos the annotation @JsonSubTypes.

Example:

```
{
    "type" : "object",
    "deserializationClassProperty":{
        "propertyName":"discriminatorValue",
        "children":
            [
                {
                    "className": "ChildArraySchema1",
                    "value": "OBJ_1"
                },
                {
                    "className": "ChildArraySchema2",
                    "value": "OBJ_2"
                }
            ]
    },
    "properties" : {
        "propertyOfParent" : {
            "type" : "string"
        },
        "deserializationClassProperty":{
            "type" : "string"
        }
    }
}
```

The result will be:
```
@Generated("org.jsonschema2pojo")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "discriminatorValue")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChildArraySchema1.class, name = "OBJ_1"),
    @JsonSubTypes.Type(value = ChildArraySchema2.class, name = "OBJ_2")
})
@JsonPropertyOrder({
    "discriminatorValue"
})
public class ValidationRQRDTO {

    @JsonProperty("discriminatorValue")
    private String discriminatorValue;
    ....
}

```