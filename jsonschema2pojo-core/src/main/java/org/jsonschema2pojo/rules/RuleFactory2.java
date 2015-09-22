/**
 * 
 */
package org.jsonschema2pojo.rules;

import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.SchemaStore2;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.rules.SchemaRule;
import org.jsonschema2pojo.util.ParcelableHelper;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

/**
 * @author Labi0@github.com
 *
 */
public class RuleFactory2 extends RuleFactory {

  /**
   * 
   */
  public RuleFactory2() {
    this(new DefaultGenerationConfig(), new Jackson2Annotator(), new SchemaStore2());
  }

  /**
   * @param generationConfig
   * @param annotator
   * @param schemaStore
   */
  public RuleFactory2(GenerationConfig generationConfig, Annotator annotator,
      SchemaStore schemaStore) {
    super(generationConfig, annotator, schemaStore);
    // TODO Auto-generated constructor stub
  }
  
  

  @Override
  public Rule<JPackage, JType> getObjectRule() {
    // TODO Auto-generated method stub
    return new ObjectRule2(this, new ParcelableHelper());
  }

  @Override
  public Rule<JClassContainer, JType> getSchemaRule() {
    return new SchemaRule2(this);
  }

}
