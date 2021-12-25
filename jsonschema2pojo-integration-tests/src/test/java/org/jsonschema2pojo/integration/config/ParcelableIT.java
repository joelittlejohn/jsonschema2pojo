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

import android.os.Parcel;
import android.os.Parcelable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.jsonschema2pojo.integration.util.ParcelUtils.*;

//@RunWith(RobolectricTestRunner.class)
//@Config(manifest=Config.NONE, sdk=23)
@Disabled("need to replace Roboleptic with something else")
public class ParcelableIT extends Jsonschema2PojoTestBase {

    @Test
    public void parcelableTreeIsParcelable() throws ClassNotFoundException, IOException {
        Class<?> parcelableType = generateAndCompile("/schema/parcelable/parcelable-schema.json", "com.example",
                config("parcelable", true))
                .loadClass("com.example.ParcelableSchema");

        Parcelable instance = (Parcelable) new ObjectMapper().readValue(ParcelableIT.class.getResourceAsStream("/schema/parcelable/parcelable-data.json"), parcelableType);
        String key = "example";
        Parcel parcel = writeToParcel(instance, key);
        Parcelable unparceledInstance = readFromParcel(parcel, parcelableType, key);

        assertThat(instance, is(equalTo(unparceledInstance)));
    }

    @Test
    public void parcelableTypeDoesNotHaveAnyDuplicateImports() throws ClassNotFoundException, IOException {
        generate("/schema/parcelable/parcelable-schema.json", "com.example", config("parcelable", true));
        File generated = generated("com/example/ParcelableSchema.java");
        String content = FileUtils.readFileToString(generated);

        Matcher m = Pattern.compile("(import [^;]+);").matcher(content);
        while (m.find()) {
            String importString = m.group();
            assertThat(StringUtils.countMatches(content, importString), is(1));
        }
    }

    @Test
    public void parcelableSuperclassIsUnparceled() throws ClassNotFoundException, IOException {
        // Explicitly set includeConstructors to false if default value changes in the future
        Class<?> parcelableType = generateAndCompile("/schema/parcelable/parcelable-superclass-schema.json", "com.example",
                config("parcelable", true, "includeConstructors", false))
                .loadClass("com.example.ParcelableSuperclassSchema");

        Parcelable instance = (Parcelable) new ObjectMapper().readValue(ParcelableIT.class.getResourceAsStream("/schema/parcelable/parcelable-superclass-data.json"), parcelableType);
        Parcel parcel = parcelableWriteToParcel(instance);
        Parcelable unparceledInstance = parcelableReadFromParcel(parcel, parcelableType, instance);

        assertThat(instance, is(equalTo(unparceledInstance)));
    }

    @Test
    public void parcelableDefaultConstructorDoesNotConflict() throws ClassNotFoundException, IOException {
        Class<?> parcelableType = generateAndCompile("/schema/parcelable/parcelable-superclass-schema.json", "com.example",
                config("parcelable", true, "includeConstructors", true))
                .loadClass("com.example.ParcelableSuperclassSchema");

        Parcelable instance = (Parcelable) new ObjectMapper().readValue(ParcelableIT.class.getResourceAsStream("/schema/parcelable/parcelable-superclass-data.json"), parcelableType);
        Parcel parcel = parcelableWriteToParcel(instance);
        Parcelable unparceledInstance = parcelableReadFromParcel(parcel, parcelableType, instance);

        assertThat(instance, is(equalTo(unparceledInstance)));
    }
}
