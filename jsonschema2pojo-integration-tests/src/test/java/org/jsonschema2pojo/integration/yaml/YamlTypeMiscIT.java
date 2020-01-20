/*
 * Copyright © 2010-2017 Nokia
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

package org.jsonschema2pojo.integration.yaml;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertThat;

public class YamlTypeMiscIT {
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void maximumGreaterThanIntegerMaxCausesIntegersToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/yaml/type/integerWithLongMaximumAsLong.yaml", "com.example", config("sourceType", "yamlschema"))
                .loadClass("com.example.IntegerWithLongMaximumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Long"));

    }

    @Test
    public void maximumGreaterThanIntegerMaxCausesIntegersToBecomePrimitiveLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/yaml/type/integerWithLongMaximumAsLong.yaml", "com.example", config("usePrimitives", true, "sourceType", "yamlschema"))
                .loadClass("com.example.IntegerWithLongMaximumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("long"));

    }

    @Test
    public void minimumLessThanIntegerMinCausesIntegersToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/yaml/type/integerWithLongMinimumAsLong.yaml", "com.example", config("sourceType", "yamlschema"))
                .loadClass("com.example.IntegerWithLongMinimumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Long"));

    }

    @Test
    public void minimumLessThanIntegerMinCausesIntegersToBecomePrimitiveLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/yaml/type/integerWithLongMinimumAsLong.yaml", "com.example", config("usePrimitives", true, "sourceType", "yamlschema"))
                .loadClass("com.example.IntegerWithLongMinimumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("long"));

    }

    @Test
    public void useLongIntegersParameterCausesIntegersToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/yaml/type/integerAsLong.yaml", "com.example", config("useLongIntegers", true, "sourceType", "yamlschema"))
                .loadClass("com.example.IntegerAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Long"));

    }

    @Test
    public void useLongIntegersParameterCausesPrimitiveIntsToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/yaml/type/integerAsLong.yaml", "com.example", config("useLongIntegers", true, "usePrimitives", true, "sourceType", "yamlschema"))
                .loadClass("com.example.IntegerAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("long"));
    }

    @Test
    public void useDoubleNumbersFalseCausesNumbersToBecomeFloats() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithDoubleProperty = schemaRule.generateAndCompile("/schema/yaml/type/numberAsFloat.yaml", "com.example", config("useDoubleNumbers", false, "sourceType", "yamlschema"))
                .loadClass("com.example.NumberAsFloat");

        Method getterMethod = classWithDoubleProperty.getMethod("getFloatProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Float"));

    }

    @Test
    public void useDoubleNumbersFalseCausesPrimitiveNumbersToBecomeFloats() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithDoubleProperty = schemaRule.generateAndCompile("/schema/yaml/type/numberAsFloat.yaml", "com.example", config("useDoubleNumbers", false, "usePrimitives", true, "sourceType", "yamlschema"))
                .loadClass("com.example.NumberAsFloat");

        Method getterMethod = classWithDoubleProperty.getMethod("getFloatProperty");

        assertThat(getterMethod.getReturnType().getName(), is("float"));
    }

    @Test
    public void unionTypesChooseFirstTypePresent() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        Class<?> classWithUnionProperties = schemaRule.generateAndCompile("/schema/yaml/type/unionTypes.yaml", "com.example", config("sourceType", "yamlschema")).loadClass("com.example.UnionTypes");

        Method booleanGetter = classWithUnionProperties.getMethod("getBooleanProperty");

        assertThat(booleanGetter.getReturnType().getName(), is("java.lang.Boolean"));

        Method stringGetter = classWithUnionProperties.getMethod("getStringProperty");

        assertThat(stringGetter.getReturnType().getName(), is("java.lang.String"));

        Method integerGetter = classWithUnionProperties.getMethod("getIntegerProperty");

        assertThat(integerGetter.getReturnType().getName(), is("java.lang.Integer"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void typeNameConflictDoesNotCauseTypeReuse() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithNameConflict = schemaRule.generateAndCompile("/schema/yaml/type/typeNameConflict.yaml", "com.example", config("sourceType", "yamlschema")).loadClass("com.example.TypeNameConflict");

        Method getterMethod = classWithNameConflict.getMethod("getTypeNameConflict");

        assertThat((Class) getterMethod.getReturnType(), is(not((Class) classWithNameConflict)));
    }

}
