/**
 * Copyright © 2010-2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo;

import static org.apache.commons.lang3.StringUtils.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class SchemaStore {

    protected final Map<URI, Schema> schemas = new HashMap<>();

    protected final FragmentResolver fragmentResolver = new FragmentResolver();
    protected final ContentResolver contentResolver;
    protected final RuleLogger logger;

    public SchemaStore() {
        this.contentResolver = new ContentResolver();
        this.logger = new NoopRuleLogger();
    }

    public SchemaStore(ContentResolver contentResolver, RuleLogger logger) {
        this.contentResolver = contentResolver;
        this.logger = logger;
    }

    /**
     * Create or look up a new schema which has the given ID and read the
     * contents of the given ID as a URL. If a schema with the given ID is
     * already known, then a reference to the original schema will be returned.
     *
     * @param id                        the id of the schema being created
     * @param refFragmentPathDelimiters A string containing any characters
     *                                  that should act as path delimiters when resolving $ref fragments.
     * @param parent
     * @return a schema object containing the contents of the given path
     */
    public synchronized Schema create(URI id, String refFragmentPathDelimiters, Schema parent) {

        URI normalizedId = id.normalize();

        if (!schemas.containsKey(normalizedId)) {

            URI baseId = removeFragment(id).normalize();
            if (!schemas.containsKey(baseId)) {
                logger.debug("Reading schema: " + baseId);
                final JsonNode baseContent = contentResolver.resolve(baseId);
                schemas.put(baseId, new Schema(baseId, baseContent, parent));
            }

            final Schema baseSchema = schemas.get(baseId);
            if (normalizedId.toString().contains("#")) {
                JsonNode childContent = fragmentResolver.resolve(baseSchema.getContent(), '#' + id.getFragment(), refFragmentPathDelimiters);
                schemas.put(normalizedId, new Schema(normalizedId, childContent, parent != null ? parent : baseSchema));
            }
        }

        return schemas.get(normalizedId);
    }

    protected URI removeFragment(URI id) {
        return URI.create(substringBefore(id.toString(), "#"));
    }

    /**
     * Create or look up a new schema using the given schema as a parent and the
     * path as a relative reference. If a schema with the given parent and
     * relative path is already known, then a reference to the original schema
     * will be returned.
     *
     * @param parent
     *            the schema which is the parent of the schema to be created.
     * @param path
     *            the relative path of this schema (will be used to create a
     *            complete URI by resolving this path against the parent
     *            schema's id)
     * @param refFragmentPathDelimiters A string containing any characters
     *                                  that should act as path delimiters when resolving $ref fragments.
     * @return a schema object containing the contents of the given path
     */
    @SuppressWarnings("PMD.UselessParentheses")
    public Schema create(Schema parent, String path, String refFragmentPathDelimiters) {

        if (!path.equals("#")) {
            // if path is an empty string then resolving it below results in jumping up a level. e.g. "/path/to/file.json" becomes "/path/to"
            path = stripEnd(path, "#?&/");
        }

        // encode the fragment for any funny characters
        if (path.contains("#")) {
            String pathExcludingFragment = substringBefore(path, "#");
            String fragment = substringAfter(path, "#");
            URI fragmentURI;
            try {
                fragmentURI = new URI(null, null, fragment);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid fragment: " + fragment + " in path: " + path);
            }
            path = pathExcludingFragment + "#" + fragmentURI.getRawFragment();
        }

        URI id = (parent == null || parent.getId() == null) ? URI.create(path) : parent.getId().resolve(path);

        String stringId = id.toString();
        if (stringId.endsWith("#")) {
            try {
                id = new URI(stripEnd(stringId, "#"));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Bad path: " + stringId);
            }
        }

        if (selfReferenceWithoutParentFile(parent, path) || substringBefore(stringId, "#").isEmpty()) {
            JsonNode parentContent = parent.getGrandParent().getContent();

            if (schemas.containsKey(id)) {
                return schemas.get(id);
            } else {
                Schema schema = new Schema(id, fragmentResolver.resolve(parentContent, path, refFragmentPathDelimiters), parent.getGrandParent());
                schemas.put(id, schema);
                return schema;
            }
        }

        return create(id, refFragmentPathDelimiters, parent);
    }

    /**
     * Creates or looks up a new property schema using the given schema as a parent.
     *
     * @param parentSchema parent schema to start the resolution with
     * @param schemaPropertyName name of the field to create a schema for
     * @param refFragmentPathDelimiters A string containing any characters
     *                                  that should act as path delimiters when resolving $ref fragments.
     * @return a schema object for the property
     */
    public Schema createPropertySchema(Schema parentSchema, String schemaPropertyName, String refFragmentPathDelimiters) {
        String pathToProperty;
        if (parentSchema.getId() == null || parentSchema.getId().getFragment() == null) {
            pathToProperty = "#/properties/" + JsonPointerUtils.encodeReferenceToken(schemaPropertyName);
        } else {
            pathToProperty =
                    "#" + parentSchema.getId().getFragment() + "/properties/" +
                            JsonPointerUtils.encodeReferenceToken(schemaPropertyName);
        }

        return create(parentSchema, pathToProperty, refFragmentPathDelimiters);
    }

    protected boolean selfReferenceWithoutParentFile(Schema parent, String path) {
        return parent != null && (parent.getId() == null || parent.getId().toString().startsWith("#/")) && path.startsWith("#");
    }

    public synchronized void clearCache() {
        schemas.clear();
    }

}
