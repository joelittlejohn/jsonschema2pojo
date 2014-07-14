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

package org.jsonschema2pojo;

import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jsonschema2pojo.FileCodeWriterWithEncoding;
import org.jsonschema2pojo.exception.GenerationException;
import org.jsonschema2pojo.rules.RuleFactory;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;

public class Jsonschema2Pojo {
    
    public static class PackageBuilder {
        List<File> sourceDirs = new ArrayList<File>();
        String packageName;
        
        public PackageBuilder addSourceDir( File sourceDir ) {
            this.sourceDirs.add(sourceDir);
            return this;
        }
        
        public PackageBuilder withPackageName( String packageName ) {
            this.packageName = packageName;
            return this;
        }
    }
    
    static String[] sortPaths( String[] paths ) {
        Arrays.sort(paths);
        return paths;
    }

    
    //
    // Start of legacy implementation.
    //
    
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
        SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, getAnnotator(config), new SchemaStore(), createPackageMapper(config)), new SchemaGenerator());

        JCodeModel codeModel = new JCodeModel();

        if (config.isRemoveOldOutput()) {
            removeOldOutput(config.getTargetDirectory());
        }

        for (Iterator<File> sources = config.getSource(); sources.hasNext();) {
            File source = sources.next();

            if (source.isDirectory()) {
                generateRecursive(config, mapper, codeModel, Arrays.asList(source.listFiles(config.getFileFilter())));
            } else {
                mapper.generate(codeModel, getNodeName(source), source.toURI().toURL());
            }
        }

        if (config.getTargetDirectory().exists() || config.getTargetDirectory().mkdirs()) {
            CodeWriter sourcesWriter = new FileCodeWriterWithEncoding(config.getTargetDirectory(), config.getOutputEncoding());
            CodeWriter resourcesWriter = new FileCodeWriterWithEncoding(config.getTargetDirectory(), config.getOutputEncoding());
            codeModel.build(sourcesWriter, resourcesWriter);
        } else {
            throw new GenerationException("Could not create or access target directory " + config.getTargetDirectory().getAbsolutePath());
        }
    }


    private static void generateRecursive(GenerationConfig config, SchemaMapper mapper, JCodeModel codeModel, List<File> schemaFiles) throws FileNotFoundException, IOException {
        Collections.sort(schemaFiles);

        for (File child : schemaFiles) {
            if (child.isFile()) {
                mapper.generate(codeModel, getNodeName(child), child.toURI().toURL());
            } else {
                generateRecursive(config, mapper, codeModel, Arrays.asList(child.listFiles(config.getFileFilter())));
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

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private static void delete(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                delete(child);
            }
        }
        f.delete();
    }

    private static Annotator getAnnotator(GenerationConfig config) {
        AnnotatorFactory factory = new AnnotatorFactory();
        return factory.getAnnotator(
                factory.getAnnotator(config.getAnnotationStyle()),
                factory.getAnnotator(config.getCustomAnnotator()));
    }

    private static String getNodeName(File file) {
        return substringBeforeLast(file.getName(), ".");
    }
    
    private static PackageMapper createPackageMapper(GenerationConfig config) throws IOException {
        PackageMapper mapper = new PackageMapper();
        
        for( Iterator<File> i = config.getSource(); i.hasNext(); ) {
            File source = i.next();
            mapper.withPackageMapping(source, config.getTargetPackage());
        }
        
        return mapper;
    }
}
