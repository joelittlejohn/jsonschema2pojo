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

package org.jsonschema2pojo.integration.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.jsonschema2pojo.maven.Jsonschema2PojoMojo;

/**
 * A plugin mojo that allows the private property values usually only set by
 * Maven to be set programatically.
 */
public class TestableJsonschema2PojoMojo extends Jsonschema2PojoMojo {

    public TestableJsonschema2PojoMojo configure(Map<String, Object> configValues) {
        
        // this could be done with reflection, if the plugin used real annotations.
        setPrivateField("sourcePaths", new String[]{});

        for (Entry<String, Object> value : configValues.entrySet()) {
            setPrivateField(value.getKey(), value.getValue());
        }

        return this;
    }

    private void setPrivateField(String name, Object value) {

        try {

            Field field = Jsonschema2PojoMojo.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(this, value);

        } catch (SecurityException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

    }

}
