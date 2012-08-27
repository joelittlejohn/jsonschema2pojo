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

package com.googlecode.jsonschema2pojo.integration.ant;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.ant.Jsonschema2PojoTask;

public class Jsonschema2PojoTaskIT {

    @Test
    public void antTaskExecutesSuccessfullyWithValidSchemas() throws URISyntaxException, ClassNotFoundException {

        File outputDirectory = invokeAntBuild("/ant/build.xml");

        ClassLoader resultsClassLoader = compile(outputDirectory);

        Class<?> generatedClass = resultsClassLoader.loadClass("com.example.WordDelimit");

        assertThat(generatedClass, is(notNullValue()));
    }

    @Test
    public void antTaskDocumentationIncludesAllProperties() throws IntrospectionException, IOException {

        String documentation = FileUtils.readFileToString(new File("../jsonschema2pojo-ant/src/site/Jsonschema2PojoTask.html"));

        for (Field f : Jsonschema2PojoTask.class.getDeclaredFields()) {
            assertThat(documentation, containsString(f.getName()));
        }

    }

    private File invokeAntBuild(String pathToBuildFile) throws URISyntaxException {
        File buildFile = new File(this.getClass().getResource(pathToBuildFile).toURI());

        File targetDirectory = createTemporaryOutputFolder();

        Project project = new Project();
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        project.setUserProperty("targetDirectory", targetDirectory.getAbsolutePath());
        project.init();

        ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference("ant.projecthelper", helper);
        helper.parse(project, buildFile);

        project.executeTarget(project.getDefaultTarget());

        return targetDirectory;
    }

}
