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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Matcher that is successful if the matched object is a file that contains the
 * given search text, or is a directory that contains a file that contains the
 * given search text.
 */
public class FileSearchMatcher extends BaseMatcher<File> {

    private final String searchText;

    /**
     * Create a new matcher with the given search text.
     * 
     * @param searchText
     *            text that the matched file should contains
     */
    public FileSearchMatcher(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public boolean matches(Object item) {
        return (item instanceof File) && isSearchTextPresent((File) item);
    }

    private boolean isSearchTextPresent(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                if (isSearchTextPresent(child)) {
                    return true;
                }
            }
            return false;
        } else {
            return isSearchTextPresentInLinesOfFile(f);
        }
    }

    private boolean isSearchTextPresentInLinesOfFile(File f) {
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(f, "UTF-8");
            while (it.hasNext()) {
                String line = it.nextLine();
                if (line.contains(searchText)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            LineIterator.closeQuietly(it);
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a file or directory that contains the text \"" + searchText + "\"");
    }

    public static Matcher<File> containsText(String searchText) {
        return new FileSearchMatcher(searchText);
    }
}
