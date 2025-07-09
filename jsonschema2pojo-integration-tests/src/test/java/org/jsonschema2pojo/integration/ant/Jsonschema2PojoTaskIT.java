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

package org.jsonschema2pojo.integration.ant;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.jsonschema2pojo.ant.Jsonschema2PojoTask;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Jsonschema2PojoTaskIT {
    
    @RegisterExtension public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void antTaskExecutesSuccessfullyWithValidSchemas() throws URISyntaxException, ClassNotFoundException {

        invokeAntBuild("/ant/build.xml");

        ClassLoader resultsClassLoader = schemaRule.compile(buildCustomClasspath());

        Class<?> generatedClass = resultsClassLoader.loadClass("com.example.WordDelimit");

        assertThat(generatedClass, is(notNullValue()));
    }

    /**
     * This test uses the ant 'classpath' config and the schemas refer to a
     * class from a custom classpath element. This should result in the custom
     * classpath element being read and no new type being generated. To test the
     * result, we need to compile with the same custom classpath.
     */
    private List<File> buildCustomClasspath() {
        return Collections.singletonList(new File("target/custom-libs/de.flapdoodle.embedmongo-1.18.jar"));
    }

    @Test
    public void antTaskDocumentationIncludesAllProperties() throws IOException {

        String documentation = FileUtils.readFileToString(new File("../jsonschema2pojo-ant/src/site/Jsonschema2PojoTask.html"), UTF_8);

        for (Field f : Jsonschema2PojoTask.class.getDeclaredFields()) {
            assertThat(documentation, containsString(">"+f.getName()+"<"));
        }

    }

    private void invokeAntBuild(String pathToBuildFile) throws URISyntaxException {
        File buildFile = new File(this.getClass().getResource(pathToBuildFile).toURI());

        Project project = new Project();
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        project.setUserProperty("targetDirectory", schemaRule.getGenerateDir().getAbsolutePath());
        project.init();

        ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference("ant.projecthelper", helper);
        helper.parse(project, buildFile);

        project.executeTarget(project.getDefaultTarget());
    }

}
