package org.jsonschema2pojo.integration;

import com.sun.codemodel.JMod;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.generateAndCompile;
import static org.junit.Assert.assertEquals;

public class ConstructorsIT {

    private static Class typeWithRequired;
    private static Class typeWithoutRequired;

    private static Map<String, Object> config;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void generateAndCompileConstructorClasses() throws ClassNotFoundException {

        config = config("propertyWordDelimiters", "_",
                "includeConstructors", true
        );
        typeWithRequired = generateAndLoadClass(
                "/schema/constructors/requiredPropertyConstructors.json",
                "com.example",
                "com.example.RequiredPropertyConstructors",
                config);

        typeWithoutRequired = generateAndLoadClass(
                "/schema/constructors/noRequiredPropertiesConstructor.json",
                "com.example",
                "com.example.NoRequiredPropertiesConstructor",
                config);
    }

    private static Class generateAndLoadClass(String schemaPath, String packageName, String className, Map<String, Object> config) throws ClassNotFoundException {
        ClassLoader resultsClassLoader = generateAndCompile(schemaPath, packageName,
                config);

        return resultsClassLoader.loadClass(className);
    }

    @Test
    public void testCreatesPublicNoArgsConstructor() throws Exception {
        Constructor constructor = typeWithRequired.getConstructor();

        assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
    }

    public static void assertHasModifier(int modifier, int modifiers, String modifierName) {
        assertEquals(
                "Expected the bit " + modifierName + " (" + modifier + ")" + " to be set but got: " + modifiers,
                modifier, modifier & modifiers);
    }

    @Test
    public void testCreatesConstructorWithRequiredParams() throws Exception {
        Constructor constructor = getArgsConstructor();

        assertHasModifier(JMod.PUBLIC, constructor.getModifiers(), "public");
    }

    public Constructor getArgsConstructor() throws NoSuchMethodException {
        return typeWithRequired.getConstructor(String.class, Integer.class, Boolean.class);
    }

    @Test
    public void testConstructorAssignsFields() throws Exception {
        Object instance = getArgsConstructor().newInstance("type", 5, true);

        assertEquals("type", invokeGeneratedMethod("getType", instance));
        assertEquals(5, invokeGeneratedMethod("getId", instance));
        assertEquals(true, invokeGeneratedMethod("getHasTickets", instance));
    }

    private Object invokeGeneratedMethod(String method, Object instance) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return typeWithRequired.getMethod(method).invoke(instance);
    }

    @Test
    public void testNoConstructorWithoutRequiredParams() throws Exception {
        assertHasOnlyDefaultConstructor(typeWithoutRequired);
    }

    private static void assertHasOnlyDefaultConstructor(Class cls) {
        Constructor[] constructors = cls.getConstructors();

        assertEquals(constructors.length, 1);

        assertEquals("Expected " + typeWithoutRequired + " to only have the default, no-args constructor",
                0, constructors[0].getParameterTypes().length);
    }

    @Test
    public void testDoesntGenerateConstructorsWithoutConfig() throws Exception {

        Class noConstructors = generateAndLoadClass(
                "/schema/constructors/requiredPropertyConstructors.json",
                "com.example",
                "com.example.RequiredPropertyConstructors",
                config("propertyWordDelimiters", "_",
                        "includeConstructors", false
                ));
        assertHasOnlyDefaultConstructor(noConstructors);
    }
}
