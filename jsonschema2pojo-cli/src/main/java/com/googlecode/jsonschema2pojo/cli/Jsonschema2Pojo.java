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

package com.googlecode.jsonschema2pojo.cli;

import static org.apache.commons.lang.StringUtils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.googlecode.jsonschema2pojo.GenerationConfig;
import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.SchemaMapperImpl;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.googlecode.jsonschema2pojo.rules.RuleFactoryImpl;
import com.sun.codemodel.JCodeModel;

/**
 * Main class, providing a command line interface for jsonschema2pojo.
 */
public final class Jsonschema2Pojo {

    private Jsonschema2Pojo() {
    }

    /**
     * Main method, entry point for the application when invoked via the command
     * line. Arguments are expected in POSIX format, invoke with --help for
     * details.
     * 
     * @param args
     *            Incoming arguments from the command line
     * @throws FileNotFoundException
     *             if the paths specified on the command line are not found
     * @throws IOException
     *             if the application is unable to read data from the paths
     *             specified
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        Arguments arguments = new Arguments().parse(args);

        generate(arguments);
    }

    /**
     * Reads the contents of the given source and initiates schema generation.
     * 
     * @param config
     *            the configuration options (including source and target paths,
     *            and other behavioural options) that will control code
     *            generation
     * @throws FileNotFoundException
     *             if the source path is not found
     * @throws IOException
     *             if the application is unable to read data from the source
     */
    public static void generate(GenerationConfig config) throws FileNotFoundException, IOException {

        SchemaMapper mapper = new SchemaMapperImpl(new RuleFactoryImpl(config));

        JCodeModel codeModel = new JCodeModel();

        for (Iterator<File> source = config.getSource(); source.hasNext();) {
            File sourceFile = source.next();

            if (sourceFile.isDirectory()) {

                List<File> schemaFiles = Arrays.asList(sourceFile.listFiles());
                Collections.sort(schemaFiles);

                for (File child : schemaFiles) {
                    if (child.isFile()) {
                        mapper.generate(codeModel, getNodeName(child), defaultString(config.getTargetPackage()), child.toURI()
                                .toURL());
                    }
                }
            } else {
                mapper.generate(codeModel, getNodeName(sourceFile), defaultString(config.getTargetPackage()), sourceFile.toURI().toURL());
            }
        }

        if (config.getTargetDirectory().exists() || config.getTargetDirectory().mkdirs()) {
            codeModel.build(config.getTargetDirectory());
        } else {
            throw new GenerationException("Could not create or access target directory " + config.getTargetDirectory().getAbsolutePath());
        }
    }

    private static String getNodeName(File file) {
        return substringBeforeLast(file.getName(), ".");
    }

}
