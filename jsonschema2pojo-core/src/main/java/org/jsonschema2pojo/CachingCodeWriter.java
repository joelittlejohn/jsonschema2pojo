/**
 * Copyright Â© 2010-2014 Nokia
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FilterCodeWriter;

/**
 * This {@link CodeWriter} variant won't write out files if the contents of the current file and the desired file are the same.
 * For example, useful for prevent recompilation of all files in Maven.
 */
public class CachingCodeWriter extends FilterCodeWriter {

    private GenerationConfig config;
    private List<RequestedFile> requestedSourceFiles = new ArrayList<RequestedFile>();

    public CachingCodeWriter(GenerationConfig config, CodeWriter core) {
        super(core);
        this.config = config;
    }

    @Override
    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        RequestedFile binary = new RequestedFile(pkg, fileName);
        requestedSourceFiles.add(binary);
        return binary.getOutput();
    }

    @Override
    public void close() throws IOException {

        nextRequestedSource:
        for (RequestedFile source : requestedSourceFiles) {
            final File dir;
            if (source.getPkg().isUnnamed()) {
                dir = config.getTargetDirectory();
            } else {
                dir = new File(config.getTargetDirectory(), source.getPkg().name().replace('.', File.separatorChar));
            }

            File targetFile = new File(dir, source.getFileName());

            if (targetFile.exists()) {

                BufferedReader currentFile = null;
                try {
                    currentFile = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), config.getOutputEncoding()));

                    BufferedReader requestedFile = new BufferedReader(new StringReader(source.getOutput().toString()));

                    if (IOUtils.contentEqualsIgnoreEOL(currentFile, requestedFile)) {
                        continue nextRequestedSource;
                    }

                } finally {
                    if (currentFile != null) {
                        currentFile.close();
                    }
                }
            }

            generateOutput(source);

        }

        super.close();
    }

    private void generateOutput(RequestedFile source) throws IOException {
        Writer output = super.openSource(source.getPkg(), source.getFileName());
        IOUtils.copy(new StringReader(source.getOutput().toString()), output);
        output.close();
    }

    private static class RequestedFile {
        private final JPackage pkg;
        private final String fileName;
        private final StringWriter output;
        public RequestedFile(JPackage pkg, String fileName) {
            this.pkg = pkg;
            this.fileName = fileName;
            output = new StringWriter();
        }
        public JPackage getPkg() {
            return pkg;
        }
        public String getFileName() {
            return fileName;
        }
        public StringWriter getOutput() {
            return output;
        }
    }
}
