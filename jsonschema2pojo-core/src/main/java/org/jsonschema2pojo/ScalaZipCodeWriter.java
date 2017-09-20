/**
 * Copyright © 2010-2017 Nokia
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

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.mysema.scalagen.ConversionSettings;
import com.mysema.scalagen.Converter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.ZipCodeWriter;

public class ScalaZipCodeWriter extends ZipCodeWriter {

    public ScalaZipCodeWriter(OutputStream target) {
        super(target);
    }

    @Override
    public OutputStream openBinary(final JPackage pkg, String fileName) throws IOException {
        final ByteArrayOutputStream javaSourceStream = new ByteArrayOutputStream();

        final String scalaFileName = fileName.replaceAll("\\.java$", ".scala");

        return new FilterOutputStream(javaSourceStream) {
            @Override
            public void close() throws IOException {
                super.close();

                final String javaSource = new String(javaSourceStream.toByteArray(), "utf-8");
                final String scalaSource = Converter.instance210().convert(javaSource, new ConversionSettings(false));

                OutputStream parentStream = ScalaZipCodeWriter.super.openBinary(pkg, scalaFileName);
                parentStream.write(scalaSource.getBytes("utf-8"));
            }
        };
    }

}
