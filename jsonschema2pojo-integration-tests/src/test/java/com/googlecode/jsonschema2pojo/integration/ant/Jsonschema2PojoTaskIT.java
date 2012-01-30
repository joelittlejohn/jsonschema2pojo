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

import java.io.File;
import java.net.URISyntaxException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.Test;

public class Jsonschema2PojoTaskIT {
    
    @Test
    public void antTaskExecutesSuccessfullyWithValidSchema() throws URISyntaxException, ClassNotFoundException {
        
        File outputDirectory = invokeAntBuild("/ant/build.xml");
        
        ClassLoader resultsClassLoader = compile(outputDirectory);
        
        Class<?> generatedClass = resultsClassLoader.loadClass("com.example.WordDelimit");
        
        assertThat(generatedClass, is(notNullValue()));
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
