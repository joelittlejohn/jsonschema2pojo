/**
 * Copyright © 2010-2014 Nokia
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

package org.jsonschema2pojo.maven;

import org.jsonschema2pojo.maven.MatchPatternsFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class MatchPatternsFileFilterTest {

    File basedir;
    MatchPatternsFileFilter fileFilter;

    @Before
    public void setUp() {
        basedir = new File("./src/test/resources/filtered/schema");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldIncludeAllIfEmpty() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = basedir.listFiles(fileFilter);

        assertThat("all of the files were found.", asList(files),
                hasItems(
                        equalTo(file("sub1")),
                        equalTo(file("excluded")),
                        equalTo(file("example.json")),
                        equalTo(file("README.md"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldIncludeMatchesAndDirectoriesWhenIncluding() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .addIncludes(asList("**/*.json"))
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = basedir.listFiles(fileFilter);

        assertThat("all of the files were found.", asList(files),
                hasItems(
                        equalTo(file("sub1")),
                        equalTo(file("excluded")),
                        equalTo(file("example.json"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldIncludeMatchesAndDirectoriesWhenIncludingAndDefaultExcludes() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .addIncludes(asList("**/*.json"))
                .addDefaultExcludes()
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = basedir.listFiles(fileFilter);

        assertThat("all of the files were found.", asList(files),
                hasItems(
                        equalTo(file("sub1")),
                        equalTo(file("excluded")),
                        equalTo(file("example.json"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNoIncludedUnmatchedFiles() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .addIncludes(asList("**/*.json"))
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = basedir.listFiles(fileFilter);

        assertThat("the markdown file was not found.", asList(files), not(hasItem(file("README.md"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNoIncludedNestedUnmatchedFiles() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .addIncludes(asList("**/*.json"))
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = new File(basedir, "sub1").listFiles(fileFilter);

        assertThat("the markdown file was not found.", asList(files), not(hasItem(file("README.md"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldExcludeNested() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .addExcludes(asList("**/*.md"))
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = new File(basedir, "sub1").listFiles(fileFilter);

        assertThat("the markdown file was not found.", asList(files), not(hasItem(file("README.md"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldExcludeDirectories() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .addExcludes(asList("**/excluded/**"))
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = basedir.listFiles(fileFilter);

        assertThat("the markdown file was not found.", asList(files), not(hasItem(file("excluded"))));
    }

    @Test
    public void ahouldNotExcludeRegularDirectoriesWithDefaultExcludes() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .addDefaultExcludes()
                .addIncludes(asList("**"))
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = basedir.listFiles(fileFilter);

        assertThat("the sub directory was not found.", asList(files), hasItem(file("excluded")));
    }

    @Test
    public void shouldExcludeSvnDirectoriesWithDefaultExcludes() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .addDefaultExcludes()
                .addIncludes(asList("**"))
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = basedir.listFiles(fileFilter);

        assertThat("the files in .svn directory were execluded.", asList(files), not(hasItems(file(".svn"))));
    }

    @Test
    public void shouldExcludeFilesInSvnDirectoriesWithDefaultExcludes() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .addDefaultExcludes()
                .addIncludes(asList("**/*.json"))
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = new File(basedir, ".svn").listFiles(fileFilter);

        assertThat("the files in .svn directory were execluded.", asList(files), not(hasItems(file("svn-file.json"))));
    }

    @Test
    public void shouldExcludeNestedFilesInSvnDirectoriesWithDefaultExcludes() throws IOException {
        fileFilter = new MatchPatternsFileFilter.Builder()
                .addDefaultExcludes()
                .addIncludes(asList("**/*.json"))
                .withSourceDirectory(basedir.getCanonicalPath())
                .build();

        File[] files = new File(basedir, ".svn/sub").listFiles(fileFilter);

        assertThat("the files in .svn directory were execluded.", asList(files), not(hasItems(file("sub-svn-file.json"))));
    }
    
    private File file(String relativePath) {
        return new File(basedir, relativePath);
    }

}
