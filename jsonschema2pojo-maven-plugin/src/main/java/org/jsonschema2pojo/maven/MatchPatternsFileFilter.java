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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.apache.maven.shared.utils.io.MatchPatterns;

import static java.util.Arrays.*;
import static java.lang.String.format;
import static java.util.regex.Pattern.quote;

/**
 * <p>A file filter that supports include and exclude patterns.</p>
 * 
 * @author Christian Trimble
 * @since 0.4.3
 */
public class MatchPatternsFileFilter implements FileFilter {
    MatchPatterns includePatterns;
    MatchPatterns excludePatterns;
    String sourceDirectory;
    boolean caseSensitive;

    /**
     * <p>Builder for MatchPatternFileFilter instances.</p>
     */
    public static class Builder {
        List<String> includes = new ArrayList<String>();
        List<String> excludes = new ArrayList<String>();
        String sourceDirectory;
        boolean caseSensitive;

        public Builder addIncludes(List<String> includes) {
            this.includes.addAll(processPatterns(includes));
            return this;
        }

        public Builder addIncludes(String... includes) {
            if (includes != null) {
                addIncludes(asList(includes));
            }
            return this;
        }

        public Builder addExcludes(List<String> excludes) {
            this.excludes.addAll(processPatterns(excludes));
            return this;
        }

        public Builder addExcludes(String... excludes) {
            if (excludes != null) {
                addExcludes(asList(excludes));
            }
            return this;
        }

        public Builder addDefaultExcludes() {
            excludes.addAll(processPatterns(asList(DirectoryScanner.DEFAULTEXCLUDES)));
            return this;
        }

        public Builder withSourceDirectory(String canonicalSourceDirectory) {
            this.sourceDirectory = canonicalSourceDirectory;
            return this;
        }

        public Builder withCaseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        public MatchPatternsFileFilter build() {
            if (includes.isEmpty()) {
                includes.add(processPattern("**/*"));
            }
            return new MatchPatternsFileFilter(
                    MatchPatterns.from(includes.toArray(new String[] {})),
                    MatchPatterns.from(excludes.toArray(new String[] {})),
                    sourceDirectory,
                    caseSensitive);
        }
    }

    MatchPatternsFileFilter(MatchPatterns includePatterns, MatchPatterns excludePatterns, String sourceDirectory, boolean caseSensitive) {
        this.includePatterns = includePatterns;
        this.excludePatterns = excludePatterns;
        this.sourceDirectory = sourceDirectory;
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean accept(File file) {
        try {
            String path = relativePath(file);
            return file.isDirectory() ?
                    includePatterns.matchesPatternStart(path, caseSensitive) && !excludePatterns.matches(path, caseSensitive) :
                    includePatterns.matches(path, caseSensitive) && !excludePatterns.matches(path, caseSensitive);
        } catch (IOException e) {
            return false;
        }
    }

    String relativePath(File file) throws IOException {
        String canonicalPath = file.getCanonicalPath();
        if (!canonicalPath.startsWith(sourceDirectory)) {
            throw new IOException(format("the path %s is not a decendent of the basedir %s", canonicalPath, sourceDirectory));
        }
        return canonicalPath.substring(sourceDirectory.length()).replaceAll("^" + quote(File.separator), "");
    }

    static List<String> processPatterns(List<String> patterns) {
        if (patterns == null)
            return null;
        List<String> processed = new ArrayList<String>();
        for (String pattern : patterns) {
            processed.add(processPattern(pattern));
        }
        return processed;
    }
    
    static String processPattern(String pattern) {
        return pattern
                .trim()
                .replace('/', File.separatorChar)
                .replace('\\', File.separatorChar)
                .replaceAll(quote(File.separator) + "$", File.separator + "**");
    }

}
