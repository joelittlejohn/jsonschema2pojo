/**
 * Copyright © 2010 Nokia
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.SchemaMapperImpl;
import com.sun.codemodel.JCodeModel;

public class Jsonschema2Pojo {

    public static void main(String[] args) throws FileNotFoundException, IOException {

        Arguments arguments = new Arguments().parse(args);

        generate(new File(arguments.getSource()), arguments.getPackageName(), new File(arguments.getTarget()));
    }

    public static void generate(File source, String packageName, File targetDir) throws IOException {
        SchemaMapper mapper = new SchemaMapperImpl();

        JCodeModel codeModel = new JCodeModel();

        if (source.isDirectory()) {
            for (File child : source.listFiles()) {
                if (child.isFile()) {
                    mapper.generate(codeModel, getNodeName(child), packageName, new FileInputStream(child));
                }
            }
        } else {
            mapper.generate(codeModel, getNodeName(source), packageName, new FileInputStream(source));
        }

        targetDir.mkdirs();

        codeModel.build(targetDir);
    }

    private static String getNodeName(File file) {
        return substringBeforeLast(file.getName(), ".");
    }

}
