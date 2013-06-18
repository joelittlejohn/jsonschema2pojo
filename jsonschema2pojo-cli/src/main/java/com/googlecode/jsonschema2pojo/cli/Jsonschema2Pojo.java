/**
 * Copyright © 2010-2013 Nokia
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

import com.googlecode.jsonschema2pojo.Annotator;
import com.googlecode.jsonschema2pojo.AnnotatorFactory;
import com.googlecode.jsonschema2pojo.CompositeAnnotator;
import com.googlecode.jsonschema2pojo.GenerationConfig;
import com.googlecode.jsonschema2pojo.SchemaGenerator;
import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.SchemaStore;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.googlecode.jsonschema2pojo.rules.RuleFactory;
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
        Annotator annotator = getAnnotator(config);
        SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, annotator, new SchemaStore()), new SchemaGenerator());

        JCodeModel codeModel = new JCodeModel();

        if (config.isRemoveOldOutput()) {
            removeOldOutput(config.getTargetDirectory());
        }

        for (Iterator<File> sources = config.getSource(); sources.hasNext();) {
            File source = sources.next();

            if (source.isDirectory()) {
                generateRecursive(config, mapper, codeModel, defaultString(config.getTargetPackage()), Arrays.asList(source.listFiles()));
            } else {
                mapper.generate(codeModel, getNodeName(source), defaultString(config.getTargetPackage()), source.toURI().toURL());
            }
        }

        if (config.getTargetDirectory().exists() || config.getTargetDirectory().mkdirs()) {
            codeModel.build(config.getTargetDirectory(), new NullPrintStream());
        } else {
            throw new GenerationException("Could not create or access target directory " + config.getTargetDirectory().getAbsolutePath());
        }
    }

    private static void generateRecursive(GenerationConfig config, SchemaMapper mapper, JCodeModel codeModel, String packageName, List<File> schemaFiles) throws FileNotFoundException, IOException {
        Collections.sort(schemaFiles);

        for (File child : schemaFiles) {
            if (child.isFile()) {
                mapper.generate(codeModel, getNodeName(child), defaultString(packageName), child.toURI().toURL());
            }else{
                generateRecursive(config, mapper, codeModel, packageName+"."+child.getName(), Arrays.asList(child.listFiles()));
            }
        }
    }

    private static void removeOldOutput(File targetDirectory) {
        if (targetDirectory.exists()) {
            for (File f : targetDirectory.listFiles()) {
                delete(f);
            }
        }
    }

    private static void delete(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                delete(child);
            }
        }
        f.delete();
    }

    private static Annotator getAnnotator(GenerationConfig config) {
        Annotator coreAnnotator = new AnnotatorFactory().getAnnotator(config.getAnnotationStyle());
        Annotator customAnnotator = new AnnotatorFactory().getAnnotator(config.getCustomAnnotator());
        return new CompositeAnnotator(coreAnnotator, customAnnotator);
    }

    private static String getNodeName(File file) {
        return substringBeforeLast(file.getName(), ".");
    }

}
