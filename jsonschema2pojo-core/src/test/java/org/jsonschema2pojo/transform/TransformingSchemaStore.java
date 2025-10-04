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

package org.jsonschema2pojo.transform;

import static org.apache.commons.lang3.StringUtils.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.jsonschema2pojo.ContentResolver;
import org.jsonschema2pojo.NoopRuleLogger;
import org.jsonschema2pojo.RuleLogger;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;

import com.fasterxml.jackson.databind.JsonNode;

// This would be better with composition instead of extension, but
// we don't have an interface to work with.
public class TransformingSchemaStore extends SchemaStore {
    protected final Map<URI, Schema> transformedSchemas = new HashMap<>();

    public TransformingSchemaStore() {
        super(new ContentResolver(), new NoopRuleLogger());
    }

    public TransformingSchemaStore(ContentResolver contentResolver, RuleLogger logger) {
      super(contentResolver, logger);
    }

    @Override
    public synchronized Schema create(URI id, String refFragmentPathDelimiters) {
      URI normalizedId = id.normalize();
      if( !transformedSchemas.containsKey(normalizedId) ) {
          URI baseId = removeFragment(id).normalize();
          if (!transformedSchemas.containsKey(baseId)) {
            Schema baseSchema = super.create(baseId, refFragmentPathDelimiters);
            
            // transforming code goes here, possibly loading other schemas along the way.

            Schema transformedBaseSchema = baseSchema;
            transformedSchemas.put(baseId, transformedBaseSchema);
          }

          final Schema baseSchema = transformedSchemas.get(baseId);
          if (normalizedId.toString().contains("#")) {
              JsonNode childContent = fragmentResolver.resolve(baseSchema.getContent(), '#' + id.getFragment(), refFragmentPathDelimiters);
              transformedSchemas.put(normalizedId, new Schema(normalizedId, childContent, baseSchema));
          }
      }
      return schemas.get(normalizedId);
    }

    @Override
    public synchronized Schema create(Schema parent, String path, String refFragmentPathDelimiters) {
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

            if (transformedSchemas.containsKey(id)) {
                return transformedSchemas.get(id);
            } else {
                Schema schema = new Schema(id, fragmentResolver.resolve(parentContent, path, refFragmentPathDelimiters), parent.getGrandParent());
                transformedSchemas.put(id, schema);
                return schema;
            }
        }

        return create(id, refFragmentPathDelimiters);
    }

    @Override
    public synchronized void clearCache() {
      transformedSchemas.clear();
      super.clearCache();
    }
}
