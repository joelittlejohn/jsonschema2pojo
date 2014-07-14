/**
 * Copyright ¬© 2010-2013 Nokia
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

package org.jsonschema2pojo.rules;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import org.jsonschema2pojo.GenerationConfig;

public class NameHelperTest {
    
    GenerationConfig config = mock(GenerationConfig.class);
    NameHelper helper;
    
    @Before
    public void setUp() {
        when(config.getPropertyWordDelimiters())
          .thenReturn(new char[]{ '-', ' ', '_' });
        
        helper = new NameHelper(config);
    }
    
    @Test
    public void shouldBuildPackageForTopLevelFile() {
        String packageName = helper.packageNameForSchemaPath("com.example", systemSeperator("./context.json"));
        
        assertThat("the package name for path is correct", packageName, equalTo("com.example"));
        
    }

    @Test
    public void shouldBuildRelativePathPackage() {
        String packageName = helper.packageNameForSchemaPath("com.example", systemSeperator("./context/context.json"));
        
        assertThat("the package name for path is correct", packageName, equalTo("com.example.context"));
    }

    @Test
    public void shouldBuildRelativePathPackageWithIllegalChars() {
        String packageName = helper.packageNameForSchemaPath("com.example", systemSeperator("./context-package/context.json"));
        
        assertThat("the package name for path is correct", packageName, equalTo("com.example.context_package"));
    }

    @Test
    public void shouldBuildRelativePathPackageWithSpaces() {
        String packageName = helper.packageNameForSchemaPath("com.example", systemSeperator("./context package/context.json"));
        
        assertThat("the package name for path is correct", packageName, equalTo("com.example.context_package"));
    }
    
    private static String systemSeperator( String path ) {
        return path.replaceAll("/", File.separator);
    }
}
