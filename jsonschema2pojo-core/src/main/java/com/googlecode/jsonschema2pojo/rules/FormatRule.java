package com.googlecode.jsonschema2pojo.rules;

import java.util.Date;

import org.codehaus.jackson.JsonNode;

import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class FormatRule implements SchemaRule<JPackage, JType> {

    @Override
    public JType apply(String nodeName, JsonNode node, JPackage generatableType) {

        if (node.getTextValue().equals("date-time")) {
            return generatableType.owner().ref(Date.class);

        } else if (node.getTextValue().equals("utc-millisec")) {
            return generatableType.owner().LONG;

        } else {
            return generatableType.owner().ref(String.class);
        }

    }

}
