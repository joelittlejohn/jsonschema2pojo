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

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import com.sun.codemodel.JPackage;
import com.sun.codemodel.util.UnicodeEscapeWriter;

/**
 * A writer that honours the given character encoding (workaround for an issue
 * with {@link com.sun.codemodel.util.EncoderFactory#createEncoder(String)} that
 * causes the given encoding to be ignored).
 */
public class FileCodeWriterWithEncoding extends com.sun.codemodel.writer.FileCodeWriter {

    public FileCodeWriterWithEncoding(File target, String encoding) throws IOException {
        super(target, encoding);
    }

    @Override
    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        final Writer bw = new OutputStreamWriter(openBinary(pkg, fileName), encoding);

        return new UnicodeEscapeWriter(bw) {
            private final CharsetEncoder encoder = Charset.forName(encoding).newEncoder();

            @Override
            protected boolean requireEscaping(int ch) {
                // control characters
                if (ch < 0x20 && " \t\r\n".indexOf(ch) == -1) {
                    return true;
                }
                // ASCII chars
                if (ch < 0x80) {
                    return false;
                }
                return !encoder.canEncode((char) ch);
            }
        };
    }

}
