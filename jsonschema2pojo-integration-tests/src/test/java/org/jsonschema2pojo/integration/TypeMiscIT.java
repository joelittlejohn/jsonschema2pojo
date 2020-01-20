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

package org.jsonschema2pojo.integration;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class TypeMiscIT {
    
    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void maximumGreaterThanIntegerMaxCausesIntegersToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerWithLongMaximumAsLong.json", "com.example")
                .loadClass("com.example.IntegerWithLongMaximumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Long"));

    }

    @Test
    public void maximumGreaterThanIntegerMaxCausesIntegersToBecomePrimitiveLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerWithLongMaximumAsLong.json", "com.example", config("usePrimitives", true))
                .loadClass("com.example.IntegerWithLongMaximumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("long"));

    }

    @Test
    public void minimumLessThanIntegerMinCausesIntegersToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerWithLongMinimumAsLong.json", "com.example")
                .loadClass("com.example.IntegerWithLongMinimumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Long"));

    }

    @Test
    public void minimumLessThanIntegerMinCausesIntegersToBecomePrimitiveLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerWithLongMinimumAsLong.json", "com.example", config("usePrimitives", true))
                .loadClass("com.example.IntegerWithLongMinimumAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("long"));

    }

    @Test
    public void useLongIntegersParameterCausesIntegersToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerAsLong.json", "com.example", config("useLongIntegers", true))
                .loadClass("com.example.IntegerAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Long"));

    }

    @Test
    public void useLongIntegersParameterCausesPrimitiveIntsToBecomeLongs() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithLongProperty = schemaRule.generateAndCompile("/schema/type/integerAsLong.json", "com.example", config("useLongIntegers", true, "usePrimitives", true))
                .loadClass("com.example.IntegerAsLong");

        Method getterMethod = classWithLongProperty.getMethod("getLongProperty");

        assertThat(getterMethod.getReturnType().getName(), is("long"));
    }

    @Test
    public void useDoubleNumbersFalseCausesNumbersToBecomeFloats() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithDoubleProperty = schemaRule.generateAndCompile("/schema/type/numberAsFloat.json", "com.example", config("useDoubleNumbers", false))
                .loadClass("com.example.NumberAsFloat");

        Method getterMethod = classWithDoubleProperty.getMethod("getFloatProperty");

        assertThat(getterMethod.getReturnType().getName(), is("java.lang.Float"));

    }

    @Test
    public void useDoubleNumbersFalseCausesPrimitiveNumbersToBecomeFloats() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> classWithDoubleProperty = schemaRule.generateAndCompile("/schema/type/numberAsFloat.json", "com.example", config("useDoubleNumbers", false, "usePrimitives", true))
                .loadClass("com.example.NumberAsFloat");

        Method getterMethod = classWithDoubleProperty.getMethod("getFloatProperty");

        assertThat(getterMethod.getReturnType().getName(), is("float"));
    }

    @Test
    public void unionTypesChooseFirstTypePresent() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        Class<?> classWithUnionProperties = schemaRule.generateAndCompile("/schema/type/unionTypes.json", "com.example").loadClass("com.example.UnionTypes");

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
        Class<?> classWithNameConflict = schemaRule.generateAndCompile("/schema/type/typeNameConflict.json", "com.example").loadClass("com.example.TypeNameConflict");

        Method getterMethod = classWithNameConflict.getMethod("getTypeNameConflict");

        assertThat((Class) getterMethod.getReturnType(), is(not((Class) classWithNameConflict)));
    }

}
