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
import java.util.Map;

import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.SchemaMapperImpl;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.googlecode.jsonschema2pojo.rules.RuleFactoryImpl;
import com.sun.codemodel.JCodeModel;

/**
 * Main class, providing a command line interface for jsonschema2pojo.
 */
public class Jsonschema2Pojo {
    
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
        
        generate(new File(arguments.getSource()), arguments.getPackageName(), new File(arguments.getTarget()), arguments.getBehaviourProperties());
    }
    
    /**
     * Reads the contents of the given source and initiates schema generation.
     * 
     * @param source
     *            the source file or directory from which to read JSON schema
     *            content
     * @param packageName
     *            the target package into which generated types will be placed
     *            (unless overridden by javaType property in the schema)
     * @param targetDir
     *            the output directory into which generated types will be placed
     * @param behaviourProperties
     *            additional properties which will influence code generation
     * @throws FileNotFoundException
     *             if the source path is not found
     * @throws IOException
     *             if the application is unable to read data from the source
     */
    public static void generate(File source, String packageName, File targetDir, Map<String, String> behaviourProperties) throws FileNotFoundException, IOException {

        SchemaMapper mapper = new SchemaMapperImpl(new RuleFactoryImpl(behaviourProperties));

        JCodeModel codeModel = new JCodeModel();
        
        if (source.isDirectory()) {
            for (File child : source.listFiles()) {
                if (child.isFile()) {
                    mapper.generate(codeModel, getNodeName(child), packageName, child.toURI().toURL());
                }
            }
        } else {
            mapper.generate(codeModel, getNodeName(source), packageName, source.toURI().toURL());
        }
        
        if (targetDir.exists() || targetDir.mkdirs()) {
            codeModel.build(targetDir);
        } else {
            throw new GenerationException("Could not create or access target directory " + targetDir.getAbsolutePath());
        }
    }
    
    private static String getNodeName(File file) {
        return substringBeforeLast(file.getName(), ".");
    }
    
}
