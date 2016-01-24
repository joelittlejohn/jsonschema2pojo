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

package org.jsonschema2pojo.cli;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArgumentsTest {

    private static final PrintStream SYSTEM_OUT = System.out;
    private static final PrintStream SYSTEM_ERR = System.err;
    private final ByteArrayOutputStream systemOutCapture = new ByteArrayOutputStream();
    private final ByteArrayOutputStream systemErrCapture = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        System.setOut(new PrintStream(systemOutCapture));
        System.setErr(new PrintStream(systemErrCapture));
    }

    @After
    public void tearDown() {
        System.setOut(SYSTEM_OUT);
        System.setErr(SYSTEM_ERR);
    }

    @Test
    public void parseRecognisesValidArguments() {
        ArgsForTest args = (ArgsForTest) new ArgsForTest().parse(new String[] {
                "--source", "/home/source", "--target", "/home/target", "--package", "mypackage",
                "--generate-builders", "--use-primitives", "--omit-hashcode-and-equals", "--omit-tostring", "--include-dynamic-accessors"
        });

        assertThat(args.didExit(), is(false));
        assertThat(args.getSource().next().getFile(), endsWith("/home/source"));
        assertThat(args.getTargetDirectory(), is(theFile("/home/target")));
        assertThat(args.getTargetPackage(), is("mypackage"));
        assertThat(args.isGenerateBuilders(), is(true));
        assertThat(args.isUsePrimitives(), is(true));
        assertThat(args.isIncludeHashcodeAndEquals(), is(false));
        assertThat(args.isIncludeToString(), is(false));
        assertThat(args.isIncludeDynamicAccessors(), is(true));
    }

    @Test
    public void parseRecognisesShorthandArguments() {
        ArgsForTest args = (ArgsForTest) new ArgsForTest().parse(new String[] {
                "-s", "/home/source", "-t", "/home/target", "-p", "mypackage", "-b", "-P", "-E", "-S", "-ida"
        });

        assertThat(args.didExit(), is(false));
        assertThat(args.getSource().next().getFile(), endsWith("/home/source"));
        assertThat(args.getTargetDirectory(), is(theFile("/home/target")));
        assertThat(args.getTargetPackage(), is("mypackage"));
        assertThat(args.isGenerateBuilders(), is(true));
        assertThat(args.isUsePrimitives(), is(true));
        assertThat(args.isIncludeHashcodeAndEquals(), is(false));
        assertThat(args.isIncludeToString(), is(false));
        assertThat(args.isIncludeDynamicAccessors(), is(true));
    }

    @Test
    public void parserAcceptsHyphenWordDelimiter() {
        ArgsForTest args = (ArgsForTest) new ArgsForTest().parse(new String[] {
                "-s", "/home/source", "-t", "/home/target", "--word-delimiters", "-"
        });

        assertThat(args.getPropertyWordDelimiters(), is(new char[] { '-' }));
    }

    @Test
    public void allOptionalArgsCanBeOmittedAndDefaultsPrevail() {
        ArgsForTest args = (ArgsForTest) new ArgsForTest().parse(new String[] {
                "--source", "/home/source", "--target", "/home/target"
        });

        assertThat(args.didExit(), is(false));
        assertThat(args.getSource().next().getFile(), endsWith("/home/source"));
        assertThat(args.getTargetDirectory(), is(theFile("/home/target")));
        assertThat(args.getTargetPackage(), is(nullValue()));
        assertThat(args.isGenerateBuilders(), is(false));
        assertThat(args.isUsePrimitives(), is(false));
        assertThat(args.isIncludeHashcodeAndEquals(), is(true));
        assertThat(args.isIncludeToString(), is(true));
        assertThat(args.isIncludeDynamicAccessors(), is(false));
    }

    @Test
    public void missingArgsCausesHelp() throws IOException {
        ArgsForTest args = (ArgsForTest) new ArgsForTest().parse(new String[] {});

        assertThat(args.status, is(1));
        assertThat(new String(systemErrCapture.toByteArray(), "UTF-8"), is(containsString("--target")));
        assertThat(new String(systemErrCapture.toByteArray(), "UTF-8"), is(containsString("--source")));
        assertThat(new String(systemOutCapture.toByteArray(), "UTF-8"), is(containsString("Usage: jsonschema2pojo")));
    }

    @Test
    public void requestingHelpCausesHelp() throws IOException {
        ArgsForTest args = (ArgsForTest) new ArgsForTest().parse(new String[] { "--help" });

        assertThat(args.status, is(notNullValue()));
        assertThat(new String(systemOutCapture.toByteArray(), "UTF-8"), is(containsString("Usage: jsonschema2pojo")));
    }

    private File theFile(String path) {
        return new File(path);
    }

    private static class ArgsForTest extends Arguments {
        protected Integer status;

        @Override
        protected void exit(int status) {
            this.status = status;
        }

        protected boolean didExit() {
            return (status != null);
        }
    }
}
