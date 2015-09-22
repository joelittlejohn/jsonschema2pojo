package org.jsonschema2pojo;

import java.net.URI;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;

/**
 * Provides support for URN schemas
 * @author labi
 *
 */
public class SchemaStore2 extends SchemaStore {

  public SchemaStore2() {
    // TODO Auto-generated constructor stub
  }

  public synchronized void registerId(URI id, Schema schema) {
    schemas.put(id, schema);
  }
}
