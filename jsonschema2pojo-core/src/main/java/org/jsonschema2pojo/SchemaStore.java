/**
 * Copyright © 2010-2014 Nokia
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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class SchemaStore {

    private Map<URI, Schema> schemas = new HashMap<URI, Schema>();

    private FragmentResolver fragmentResolver = new FragmentResolver();
    private ContentResolver contentResolver = new ContentResolver();

    /**
     * Create or look up a new schema which has the given ID and read the
     * contents of the given ID as a URL. If a schema with the given ID is
     * already known, then a reference to the original schema will be returned.
     * 
     * @param id
     *            the id of the schema being created
     * @return a schema object containing the contents of the given path
     */
    public synchronized Schema create(URI id) {

        if (!schemas.containsKey(id)) {

            JsonNode content = contentResolver.resolve(removeFragment(id));

            if (id.toString().contains("#")) {
                content = fragmentResolver.resolve(content, '#' + substringAfter(id.toString(), "#"));
            }

            schemas.put(id, new Schema(id, content));
        }

        return schemas.get(id);
    }

    private URI removeFragment(URI id) {
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
     * @return a schema object containing the contents of the given path
     */
    @SuppressWarnings("PMD.UselessParentheses")
    public Schema create(Schema parent, String path) {

        if (path.equals("#")) {
            return parent;
        }
        
        path = stripEnd(path, "#?&/");

        URI id = (parent == null || parent.getId() == null) ? URI.create(path) : parent.getId().resolve(path);

        if (selfReferenceWithoutParentFile(parent, path)) {
            schemas.put(id, new Schema(id, fragmentResolver.resolve(parent.getContent(), path)));
            return schemas.get(id);
        }
        
        return create(id);

    }

    private boolean selfReferenceWithoutParentFile(Schema parent, String path) {
        return parent != null && parent.getId() == null && path.startsWith("#/");
    }

    public synchronized void clearCache() {
        schemas.clear();
    }

}
