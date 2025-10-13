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
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiPredicate;

import org.jsonschema2pojo.ContentResolver;
import org.jsonschema2pojo.NoopRuleLogger;
import org.jsonschema2pojo.RuleLogger;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;

import com.fasterxml.jackson.databind.JsonNode;

// This would be better with composition instead of extension, but
// we don't have an interface to work with.
public class TransformingSchemaStore extends SchemaStore {
    protected final Map<URI, JsonNode> schemaContent = new HashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    protected SchemaTransformer transformer;

    public TransformingSchemaStore(SchemaTransformer transformer) {
        super(new ContentResolver(), new NoopRuleLogger());
        this.transformer = transformer;
    }

    public TransformingSchemaStore(SchemaTransformer transformer, ContentResolver contentResolver, RuleLogger logger) {
      super(contentResolver, logger);
      this.transformer = transformer;
    }

    @Override
    public Schema create(URI id, String refFragmentPathDelimiters) {
      URI normalizedId = id.normalize();
      boolean isFragment = normalizedId.toString().contains("#");
      URI baseId = isFragment
        ? removeFragment(id).normalize()
        : normalizedId;

      // return the schema if we have it cached.
      readLock.lock();
      try {
        if( schemas.containsKey(normalizedId)) return schemas.get(normalizedId);
      } finally {
        readLock.unlock();
      }

      // some kind of cache update needs to happen.
      writeLock.lock();
      try {
        // make sure we realy still need the update.
        if( schemas.containsKey(normalizedId)) return schemas.get(normalizedId);

        // make sure we don't have the content and are just missing the schema entry.
        if( schemaContent.containsKey(normalizedId) ) {
          Schema schema = new Schema(normalizedId, schemaContent.get(normalizedId), null);
          schemas.put(normalizedId, schema);
          return schema;
        }

        // determine if this is a fragement and we already have the content.
        if( isFragment ) {
          if( schemas.containsKey(baseId) ) {
            Schema baseSchema = schemas.get(baseId);
            JsonNode childContent = fragmentResolver.resolve(baseSchema.getContent(), '#' + id.getFragment(), refFragmentPathDelimiters);
            Schema childSchema = new Schema(normalizedId, childContent, baseSchema);
            schemas.put(normalizedId, childSchema);
            return childSchema;
          } else if ( schemaContent.containsKey(baseId) ) {
            Schema baseSchema = new Schema(baseId, schemaContent.get(baseId), null);
            schemas.put(baseId, baseSchema);
            JsonNode childContent = fragmentResolver.resolve(baseSchema.getContent(), '#' + id.getFragment(), refFragmentPathDelimiters);
            Schema childSchema = new Schema(normalizedId, childContent, baseSchema);
            schemas.put(normalizedId, childSchema);
            return childSchema;            
          }
        }

        // We do not have the base content, so load it.
        JsonNode baseContent = contentResolver.resolve(baseId);
        schemaContent.put(baseId, baseContent);

        // process the schema content map.
        transformer.transform(schemaContent, contentResolver, refFragmentPathDelimiters);

        // populate the schema.
        Schema baseSchema = new Schema(baseId, schemaContent.get(baseId), null);
        schemas.put(baseId, baseSchema);

        if( isFragment ) {
          JsonNode childContent = fragmentResolver.resolve(baseSchema.getContent(), '#' + id.getFragment(), refFragmentPathDelimiters);
          Schema childSchema = new Schema(normalizedId, childContent, baseSchema);
          schemas.put(normalizedId, childSchema);
          return childSchema;
        } else {
          return baseSchema;
        }
      } finally {
        writeLock.unlock();
      }
    }

    @Override
    public Schema create(Schema parent, String path, String refFragmentPathDelimiters) {
      throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void clearCache() {
      writeLock.lock();
      try {
        schemas.clear();
        schemaContent.clear();
      } finally {
        writeLock.unlock();
      }
    }
}
