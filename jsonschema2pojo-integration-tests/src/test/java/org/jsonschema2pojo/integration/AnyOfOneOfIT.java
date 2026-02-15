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

package org.jsonschema2pojo.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnyOfOneOfIT {

    @RegisterExtension public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void simpleAnyOfGeneratesMarkerInterfaceAndImplementations() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/simpleAnyOf.json", "com.example");

        Class<?> parentType = cl.loadClass("com.example.SimpleAnyOf");
        Method shapeGetter = parentType.getMethod("getShape");
        Class<?> shapeInterface = shapeGetter.getReturnType();

        // The property type should be an interface
        assertThat(shapeInterface.isInterface(), is(true));

        // Child types should implement the interface
        Class<?> circle = cl.loadClass("com.example.Circle");
        Class<?> rectangle = cl.loadClass("com.example.Rectangle");
        assertThat(shapeInterface.isAssignableFrom(circle), is(true));
        assertThat(shapeInterface.isAssignableFrom(rectangle), is(true));

        // Child types have their own properties
        assertThat(circle.getDeclaredField("radius"), is(notNullValue()));
        assertThat(rectangle.getDeclaredField("width"), is(notNullValue()));
        assertThat(rectangle.getDeclaredField("height"), is(notNullValue()));
    }

    @Test
    public void anyOfWithRefGeneratesInterfaceImplementedByReferencedTypes() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/anyOfWithRef.json", "com.example");

        Class<?> parentType = cl.loadClass("com.example.AnyOfWithRef");
        Method choiceGetter = parentType.getMethod("getChoice");
        Class<?> choiceInterface = choiceGetter.getReturnType();

        assertThat(choiceInterface.isInterface(), is(true));

        Class<?> target1 = cl.loadClass("com.example.AnyOfRefTarget1");
        Class<?> target2 = cl.loadClass("com.example.AnyOfRefTarget2");
        assertThat(choiceInterface.isAssignableFrom(target1), is(true));
        assertThat(choiceInterface.isAssignableFrom(target2), is(true));
    }

    @Test
    public void simpleOneOfBehavesLikeAnyOf() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/oneOf/simpleOneOf.json", "com.example");

        Class<?> parentType = cl.loadClass("com.example.SimpleOneOf");
        Method paymentGetter = parentType.getMethod("getPayment");
        Class<?> paymentInterface = paymentGetter.getReturnType();

        assertThat(paymentInterface.isInterface(), is(true));

        Class<?> creditCard = cl.loadClass("com.example.CreditCard");
        Class<?> bankAccount = cl.loadClass("com.example.BankAccount");
        assertThat(paymentInterface.isAssignableFrom(creditCard), is(true));
        assertThat(paymentInterface.isAssignableFrom(bankAccount), is(true));

        assertThat(creditCard.getDeclaredField("cardNumber"), is(notNullValue()));
        assertThat(bankAccount.getDeclaredField("accountNumber"), is(notNullValue()));
    }

    @Test
    public void anyOfWithPrimitiveFallsBackToObject() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/anyOfWithPrimitive.json", "com.example");

        Class<?> type = cl.loadClass("com.example.AnyOfWithPrimitive");
        Method getter = type.getMethod("getValue");

        assertThat(getter.getReturnType(), is(equalTo(Object.class)));
    }

    @Test
    public void anyOfAllPrimitivesFallsBackToObject() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/anyOfAllPrimitives.json", "com.example");

        Class<?> type = cl.loadClass("com.example.AnyOfAllPrimitives");
        Method getter = type.getMethod("getValue");

        assertThat(getter.getReturnType(), is(equalTo(Object.class)));
    }

    @Test
    public void emptyAnyOfFallsBackToObject() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/emptyAnyOf.json", "com.example");

        Class<?> type = cl.loadClass("com.example.EmptyAnyOf");
        Method getter = type.getMethod("getValue");

        assertThat(getter.getReturnType(), is(equalTo(Object.class)));
    }

    @Test
    public void singleChildAnyOfGeneratesInterfaceWithOneImplementation() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/singleChildAnyOf.json", "com.example");

        Class<?> type = cl.loadClass("com.example.SingleChildAnyOf");
        Method getter = type.getMethod("getItem");
        Class<?> itemInterface = getter.getReturnType();

        assertThat(itemInterface.isInterface(), is(true));

        Class<?> onlyOption = cl.loadClass("com.example.OnlyOption");
        assertThat(itemInterface.isAssignableFrom(onlyOption), is(true));
        assertThat(onlyOption.getDeclaredField("name"), is(notNullValue()));
    }

    @Test
    public void anyOfNoExplicitTypeTreatsChildrenAsObjects() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/anyOfNoExplicitType.json", "com.example");

        Class<?> type = cl.loadClass("com.example.AnyOfNoExplicitType");
        Method getter = type.getMethod("getData");
        Class<?> dataInterface = getter.getReturnType();

        assertThat(dataInterface.isInterface(), is(true));

        Class<?> alpha = cl.loadClass("com.example.Alpha");
        Class<?> beta = cl.loadClass("com.example.Beta");
        assertThat(dataInterface.isAssignableFrom(alpha), is(true));
        assertThat(dataInterface.isAssignableFrom(beta), is(true));
    }

    @Test
    public void anyOfTopLevelGeneratesInterface() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/anyOfTopLevel.json", "com.example");

        Class<?> topLevelInterface = cl.loadClass("com.example.AnyOfTopLevel");
        assertThat(topLevelInterface.isInterface(), is(true));

        Class<?> foo = cl.loadClass("com.example.Foo");
        Class<?> bar = cl.loadClass("com.example.Bar");
        assertThat(topLevelInterface.isAssignableFrom(foo), is(true));
        assertThat(topLevelInterface.isAssignableFrom(bar), is(true));
    }

    @Test
    public void anyOfWithDiscriminatorAddsJsonTypeInfoAnnotation() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/anyOfWithDiscriminator.json", "com.example");

        Class<?> parentType = cl.loadClass("com.example.AnyOfWithDiscriminator");
        Method petGetter = parentType.getMethod("getPet");
        Class<?> petInterface = petGetter.getReturnType();

        assertThat(petInterface.isInterface(), is(true));

        // Marker interface should have @JsonTypeInfo
        JsonTypeInfo typeInfo = petInterface.getAnnotation(JsonTypeInfo.class);
        assertThat(typeInfo, is(notNullValue()));
        assertThat(typeInfo.use(), is(JsonTypeInfo.Id.NAME));
        assertThat(typeInfo.include(), is(JsonTypeInfo.As.EXISTING_PROPERTY));
        assertThat(typeInfo.property(), is("petType"));

        // Marker interface should have @JsonSubTypes
        JsonSubTypes subTypes = petInterface.getAnnotation(JsonSubTypes.class);
        assertThat(subTypes, is(notNullValue()));
        assertThat(subTypes.value().length, is(2));

        // Child types should have @JsonTypeName
        Class<?> cat = cl.loadClass("com.example.Cat");
        Class<?> dog = cl.loadClass("com.example.Dog");
        assertThat(petInterface.isAssignableFrom(cat), is(true));
        assertThat(petInterface.isAssignableFrom(dog), is(true));

        JsonTypeName catTypeName = cat.getAnnotation(JsonTypeName.class);
        assertThat(catTypeName, is(notNullValue()));
        assertThat(catTypeName.value(), is("Cat"));

        JsonTypeName dogTypeName = dog.getAnnotation(JsonTypeName.class);
        assertThat(dogTypeName, is(notNullValue()));
        assertThat(dogTypeName.value(), is("Dog"));
    }

    @Test
    public void oneOfWithDiscriminatorAddsJsonTypeInfoAnnotation() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/oneOf/oneOfWithDiscriminator.json", "com.example");

        Class<?> parentType = cl.loadClass("com.example.OneOfWithDiscriminator");
        Method vehicleGetter = parentType.getMethod("getVehicle");
        Class<?> vehicleInterface = vehicleGetter.getReturnType();

        assertThat(vehicleInterface.isInterface(), is(true));

        // Marker interface should have @JsonTypeInfo
        JsonTypeInfo typeInfo = vehicleInterface.getAnnotation(JsonTypeInfo.class);
        assertThat(typeInfo, is(notNullValue()));
        assertThat(typeInfo.use(), is(JsonTypeInfo.Id.NAME));
        assertThat(typeInfo.include(), is(JsonTypeInfo.As.EXISTING_PROPERTY));
        assertThat(typeInfo.property(), is("vehicleType"));

        // Marker interface should have @JsonSubTypes
        JsonSubTypes subTypes = vehicleInterface.getAnnotation(JsonSubTypes.class);
        assertThat(subTypes, is(notNullValue()));
        assertThat(subTypes.value().length, is(2));

        // Child types should have @JsonTypeName
        Class<?> car = cl.loadClass("com.example.Car");
        Class<?> bicycle = cl.loadClass("com.example.Bicycle");
        assertThat(vehicleInterface.isAssignableFrom(car), is(true));
        assertThat(vehicleInterface.isAssignableFrom(bicycle), is(true));

        JsonTypeName carTypeName = car.getAnnotation(JsonTypeName.class);
        assertThat(carTypeName, is(notNullValue()));
        assertThat(carTypeName.value(), is("Car"));

        JsonTypeName bicycleTypeName = bicycle.getAnnotation(JsonTypeName.class);
        assertThat(bicycleTypeName, is(notNullValue()));
        assertThat(bicycleTypeName.value(), is("Bicycle"));
    }

    @Test
    public void anyOfWithoutDiscriminatorHasNoTypeInfoAnnotations() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/simpleAnyOf.json", "com.example");

        Class<?> parentType = cl.loadClass("com.example.SimpleAnyOf");
        Method shapeGetter = parentType.getMethod("getShape");
        Class<?> shapeInterface = shapeGetter.getReturnType();

        // No discriminator, so no @JsonTypeInfo
        assertThat(shapeInterface.getAnnotation(JsonTypeInfo.class), is(nullValue()));
        assertThat(shapeInterface.getAnnotation(JsonSubTypes.class), is(nullValue()));
    }

    @Test
    public void anyOfWithDiscriminatorDeserializesToCorrectSubtype() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/anyOfWithDiscriminator.json", "com.example");

        Class<?> parentType = cl.loadClass("com.example.AnyOfWithDiscriminator");
        Class<?> cat = cl.loadClass("com.example.Cat");
        Class<?> dog = cl.loadClass("com.example.Dog");

        // Deserialize a Cat
        Object catOwner = mapper.readValue(
                "{\"pet\":{\"petType\":\"Cat\",\"huntingSkill\":\"lazy\"}}", parentType);
        Object catPet = new PropertyDescriptor("pet", parentType).getReadMethod().invoke(catOwner);
        assertThat(cat.isInstance(catPet), is(true));
        assertThat(new PropertyDescriptor("huntingSkill", cat).getReadMethod().invoke(catPet), is("lazy"));

        // Deserialize a Dog
        Object dogOwner = mapper.readValue(
                "{\"pet\":{\"petType\":\"Dog\",\"breed\":\"labrador\"}}", parentType);
        Object dogPet = new PropertyDescriptor("pet", parentType).getReadMethod().invoke(dogOwner);
        assertThat(dog.isInstance(dogPet), is(true));
        assertThat(new PropertyDescriptor("breed", dog).getReadMethod().invoke(dogPet), is("labrador"));
    }

    @Test
    public void oneOfWithDiscriminatorDeserializesToCorrectSubtype() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/oneOf/oneOfWithDiscriminator.json", "com.example");

        Class<?> parentType = cl.loadClass("com.example.OneOfWithDiscriminator");
        Class<?> car = cl.loadClass("com.example.Car");
        Class<?> bicycle = cl.loadClass("com.example.Bicycle");

        // Deserialize a Car
        Object carOwner = mapper.readValue(
                "{\"vehicle\":{\"vehicleType\":\"Car\",\"doors\":4}}", parentType);
        Object carVehicle = new PropertyDescriptor("vehicle", parentType).getReadMethod().invoke(carOwner);
        assertThat(car.isInstance(carVehicle), is(true));
        assertThat(new PropertyDescriptor("doors", car).getReadMethod().invoke(carVehicle), is(4));

        // Deserialize a Bicycle
        Object bicycleOwner = mapper.readValue(
                "{\"vehicle\":{\"vehicleType\":\"Bicycle\",\"gears\":21}}", parentType);
        Object bicycleVehicle = new PropertyDescriptor("vehicle", parentType).getReadMethod().invoke(bicycleOwner);
        assertThat(bicycle.isInstance(bicycleVehicle), is(true));
        assertThat(new PropertyDescriptor("gears", bicycle).getReadMethod().invoke(bicycleVehicle), is(21));
    }

    @Test
    public void anyOfWithDiscriminatorRoundTrips() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/anyOf/anyOfWithDiscriminator.json", "com.example");

        Class<?> parentType = cl.loadClass("com.example.AnyOfWithDiscriminator");
        Class<?> cat = cl.loadClass("com.example.Cat");

        // Deserialize, serialize, deserialize again
        Object original = mapper.readValue(
                "{\"pet\":{\"petType\":\"Cat\",\"huntingSkill\":\"expert\"}}", parentType);
        String json = mapper.writeValueAsString(original);
        Object roundTripped = mapper.readValue(json, parentType);

        Object pet = new PropertyDescriptor("pet", parentType).getReadMethod().invoke(roundTripped);
        assertThat(cat.isInstance(pet), is(true));
        assertThat(new PropertyDescriptor("huntingSkill", cat).getReadMethod().invoke(pet), is("expert"));
        assertThat(new PropertyDescriptor("petType", cat).getReadMethod().invoke(pet), is("Cat"));
    }

}
