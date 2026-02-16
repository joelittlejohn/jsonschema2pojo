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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.jsonschema2pojo.integration.util.ParcelUtils.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.robolectric.interceptors.AndroidInterceptors;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.ClassDetails;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.internal.bytecode.UrlResourceProvider;
import org.robolectric.sandbox.ShadowMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableIT {

    @RegisterExtension
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @BeforeAll
    public static void setUp() {
        final Interceptors interceptors = new Interceptors(AndroidInterceptors.all());
        final ShadowMap shadowMap = ShadowMap.createFromShadowProviders(Lists.newArrayList(ServiceLoader.load(ShadowProvider.class)));

        InstrumentationConfiguration config = mock(InstrumentationConfiguration.class);
        when(config.shouldAcquire(anyString())).thenReturn(false);
        when(config.shouldInstrument(any(ClassDetails.class))).thenReturn(false);
        SandboxClassLoader classLoader = new SandboxClassLoader(config, new UrlResourceProvider(), new ClassInstrumentor());
        new Sandbox(classLoader).configure(new ShadowWrangler(shadowMap, ShadowMatcher.MATCH_ALL, interceptors), interceptors);
    }

    @Test
    public void parcelableTreeIsParcelable() throws ClassNotFoundException, IOException {
        Class<? extends Parcelable> parcelableType = schemaRule.generateAndCompile("/schema/parcelable/parcelable-schema.json", "com.example",
                                                                config("parcelable", true))
                .loadClass("com.example.ParcelableSchema")
                .asSubclass(Parcelable.class);
        Parcelable instance = new ObjectMapper().readValue(ParcelableIT.class.getResourceAsStream("/schema/parcelable/parcelable-data.json"), parcelableType);
        String key = "example";
        Parcel parcel = writeToParcel(instance, key);
        Parcelable unparceledInstance = readFromParcel(parcel, parcelableType, key);

        assertThat(instance, is(equalTo(unparceledInstance)));
    }

    @Test
    public void parcelableTypeDoesNotHaveAnyDuplicateImports() throws IOException {
        schemaRule.generate("/schema/parcelable/parcelable-schema.json", "com.example", config("parcelable", true));
        File generated = schemaRule.generated("com/example/ParcelableSchema.java");
        String content = FileUtils.readFileToString(generated, StandardCharsets.UTF_8);

        Matcher m = Pattern.compile("(import [^;]+);").matcher(content);
        while (m.find()) {
            String importString = m.group();
            assertThat(StringUtils.countMatches(content, importString), is(1));
        }
    }

    @Test
    public void parcelableSuperclassIsUnparceled() throws ClassNotFoundException, IOException {
        // Explicitly set includeConstructors to false if default value changes in the future
        Class<? extends Parcelable> parcelableType = schemaRule.generateAndCompile("/schema/parcelable/parcelable-superclass-schema.json", "com.example",
                config("parcelable", true, "includeConstructors", false))
                .loadClass("com.example.ParcelableSuperclassSchema")
                .asSubclass(Parcelable.class);

        Parcelable instance = new ObjectMapper().readValue(ParcelableIT.class.getResourceAsStream("/schema/parcelable/parcelable-superclass-data.json"), parcelableType);
        Parcel parcel = parcelableWriteToParcel(instance);
        Parcelable unparceledInstance = parcelableReadFromParcel(parcel, instance);

        assertThat(instance, is(equalTo(unparceledInstance)));
    }

    @Test
    public void parcelableDefaultConstructorDoesNotConflict() {
        schemaRule.generate("/schema/parcelable/parcelable-superclass-schema.json", "com.example",
                                      config("parcelable", true, "includeConstructors", true));
        // Compilation would if there are multiple constructors with the same signature
        assertDoesNotThrow(() -> schemaRule.compile());
    }

}
