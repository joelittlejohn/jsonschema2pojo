/**
 * 
 */
package org.jsonschema2pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsonschema2pojo.AbstractAnnotator;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;

/**
 * Provides miscellaneous features such as tracking the generated classes. 
 * At the end of one or more SchemaMapper::generate invocation, 
 * tracked-classes can be cleared by using the reset method.
 * 
 * @author Labi0@github.com
 *
 */
public class MiscSupport extends AbstractAnnotator {
  private List<JDefinedClass> trackedClasses = new ArrayList<JDefinedClass>();
  
  /**
   * 
   */
  public MiscSupport() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
    if(!trackedClasses.contains(clazz)) {
      trackedClasses.add(clazz);
    }
  }

  public List<JDefinedClass> getTrackedClasses() {
    return Collections.unmodifiableList(trackedClasses);
  }

  public void reset() {
    trackedClasses.clear();
  }
}
