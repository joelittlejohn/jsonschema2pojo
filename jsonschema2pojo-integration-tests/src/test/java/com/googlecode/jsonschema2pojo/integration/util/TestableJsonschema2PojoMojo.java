/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo.integration.util;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.maven.project.MavenProject;

import com.googlecode.jsonschema2pojo.maven.Jsonschema2PojoMojo;

/**
 * A plugin mojo that allows the private property values usually only set by
 * Maven to be set programatically.
 */
public class TestableJsonschema2PojoMojo extends Jsonschema2PojoMojo {

    public TestableJsonschema2PojoMojo configure(File sourceDirectory, File outputDirectory, String targetPackage, boolean generateBuilders, boolean usePrimitives, char[] wordDelimiters, MavenProject project) {

        setPrivateField("sourceDirectory", sourceDirectory);
        setPrivateField("outputDirectory", outputDirectory);
        setPrivateField("project", project);
        setPrivateField("targetPackage", targetPackage);
        setPrivateField("generateBuilders", generateBuilders);
        setPrivateField("usePrimitives", usePrimitives);
        setPrivateField("propertyWordDelimiters", new String(wordDelimiters));
        
        return this;
    }

    private void setPrivateField(String name, Object value) {

        try {

            Field field = Jsonschema2PojoMojo.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(this, value);

        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

}
