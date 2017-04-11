package org.jsonschema2pojo.integration.config;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertEquals;

public class ProcessSourceFilesBeforeDirectoriesIT
{

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void generatedClassesInCorrectPackage() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/processSourceFilesBeforeDirectories/", "com.example", config("processSourceFilesBeforeDirectories", true));

        Class generatedTypeA = resultsClassLoader.loadClass("com.example.A");
        Class generatedTypeZ = resultsClassLoader.loadClass("com.example.Z");

        Method getterTypeA = generatedTypeA.getMethod("getRefToA");
        final Class<?> returnTypeA = getterTypeA.getReturnType();

        Method getterTypeZ = generatedTypeZ.getMethod("getRefToZ");
        final Class<?> returnTypeZ = getterTypeZ.getReturnType();

        assertSamePackage(generatedTypeA,  generatedTypeZ);
        assertSamePackage(generatedTypeA,  returnTypeA);
        assertSamePackage(generatedTypeZ,  returnTypeZ);

    }

    private void assertSamePackage(Class<?> classA, Class<?> classB)
    {
        assertEquals("Expected generated classes to be in the same package", classA.getPackage(),  classB.getPackage());
    }
}
