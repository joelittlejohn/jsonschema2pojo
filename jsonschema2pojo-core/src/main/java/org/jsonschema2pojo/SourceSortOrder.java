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

package org.jsonschema2pojo;

import java.io.File;
import java.util.Comparator;

/**
 * Defines the order the source files and directories are processed in.
 */
public enum SourceSortOrder {
    /**
     * <code>OS</code> Let the OS influence the order that the source files are processed.
     */
    OS {
        @Override
        public Comparator<File> getComparator() {
            return (a, b) -> a.compareTo(b);
        }
    },

    /**
     * <code>FILES_FIRST</code> Case sensitive sort, visit the files first.  The source files are processed in a
     * breadth first sort order.
     */
    FILES_FIRST {
        @Override
        public Comparator<File> getComparator() {
            return new Comparator<File>() {
                @Override
                public int compare(final File fileA, final File fileB) {
                    if (fileA.isDirectory() && !fileB.isDirectory()) {
                        return 1;
                    }

                    if (!fileA.isDirectory() && fileB.isDirectory()) {
                        return -1;
                    }
                    return fileA.compareTo(fileB);
                }
            };
        }
    },

    /**
     * <code>SUBDIRS_FIRST</code> Case sensitive sort, visit the sub-directories before the files.  The source files
     * are processed in a depth first sort order.
     */
    SUBDIRS_FIRST {
        @Override
        public Comparator<File> getComparator() {
            return new Comparator<File>() {
                @Override
                public int compare(final File fileA, final File fileB) {
                    if (fileA.isDirectory() && !fileB.isDirectory()) {
                        return -1;
                    }

                    if (!fileA.isDirectory() && fileB.isDirectory()) {
                        return 1;
                    }
                    return fileA.compareTo(fileB);
                }
            };
        }
    };

    public abstract Comparator<File> getComparator();
}
