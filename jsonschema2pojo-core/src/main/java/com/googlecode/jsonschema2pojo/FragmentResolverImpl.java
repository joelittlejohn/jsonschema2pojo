package com.googlecode.jsonschema2pojo;

import static java.util.Arrays.*;
import static org.apache.commons.lang.StringUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

public class FragmentResolverImpl implements FragmentResolver {

    @Override
    public JsonNode resolve(JsonNode tree, String path) {

        return resolve(tree, new ArrayList<String>(asList(split(path, "#/."))));

    }

    private JsonNode resolve(JsonNode tree, List<String> path) {

        if (path.isEmpty()) {
            return tree;
        } else {
            String part = path.remove(0);

            if (tree.isArray()) {
                try {
                    int index = Integer.parseInt(part);
                    return resolve(tree.get(index), path);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Not a valid array index: " + part);
                }
            }

            if (tree.has(part)) {
                return resolve(tree.get(part), path);
            } else {
                throw new IllegalArgumentException("Path not present: " + part);
            }
        }

    }
}
