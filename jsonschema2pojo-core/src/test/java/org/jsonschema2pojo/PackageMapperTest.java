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

package org.jsonschema2pojo;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class PackageMapperTest {

    PackageMapper mapper = new PackageMapper();
    
    File namingDir = new File("./src/test/resources/schema/naming");
    File commonDir = new File(namingDir, "common");
    File common2Dir = new File(namingDir, "common2");
    File dir1 = new File(namingDir, "dir1");
    File dir2 = new File(namingDir, "dir2");
    File topLevelSchema = new File(namingDir, "top-level.json");
    File commonSchema = new File(commonDir, "common-type.json");
    File newCommonSchema = new File(common2Dir, "new-common-type.json");
    File singleRefSchema = new File(dir1, "single-ref.json");
    File multipleRefSchema = new File(dir2, "multiple-refs.json");
    
    @Test
    public void shouldReturnEmptyForNoMappings() throws IOException {
        String packageName = mapper.map(topLevelSchema);
        
        assertThat("an empty mapper produces empty packages", packageName, equalTo(""));
    
    }

    @Test
    public void shouldReturnPackageNameWhenMapped() throws IOException {
        mapper.withPackageMapping(namingDir, "com.example");
        
        String packageName = mapper.map(topLevelSchema);
        
        assertThat("the no mapping cause empty package", packageName, equalTo("com.example"));
    
    }
    
    @Test
    public void shouldAddPackageNames() throws IOException {
        mapper.withPackageMapping(namingDir, "com.example");
        
        String packageName = mapper.map(commonSchema);
        
        assertThat("nested packages are added", packageName, equalTo("com.example.common"));
        
    }
    
    @Test
    public void shouldAllowDifferentNestedPackageNames() throws IOException {
        mapper
          .withPackageMapping(namingDir, "com.example")
          .withPackageMapping(commonDir, "com.sample")
          .withPackageMapping(dir1, "com.archetype");
        
        String commonPackage = mapper.map(commonSchema);
        String dir1Package = mapper.map(singleRefSchema);
        String dir2Package = mapper.map(multipleRefSchema);
        
        assertThat("the common package was mapped", commonPackage, equalTo("com.sample"));
        assertThat("dir1 was mapped", dir1Package, equalTo("com.archetype"));
        assertThat("dir2 was mapped", dir2Package, equalTo("com.example.dir2"));
        
    }
    
    @Test
    public void shouldAllowSimilarSiblingNames() throws IOException {
        mapper
          .withPackageMapping(commonDir, "com.sample");
        
        String commonPackage = mapper.map(commonSchema);
        String newCommonPackage = mapper.map(newCommonSchema);
        
        assertThat("the common package was mapped", commonPackage, equalTo("com.sample"));
        assertThat("the common2 package was mapped", newCommonPackage, equalTo(""));
        
    }
}
