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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArgumentsTest {

    private static final String HELP_OUTPUT = 
                    "usage: jsonschema2pojo [-b] [-h] [-p <package name>] -s <arg> -t" +
    		        "\n       <directory>\n" +
                    " -b,--generate-builders        Generate builder-style methods as well as\n" +
                    "                               setters\n" +
                    " -h,--help                     Print help information and exit\n" +
                    " -p,--package <package name>   A java package used for generated types\n" +
                    " -s,--source <arg>             The source file or directory from which\n" +
                    "                               JSON Schema will be read\n" +
                    " -t,--target <directory>       The target directory into which generated\n" +
                    "                               types will be written\n";
    private static PrintStream SYSTEM_OUT = System.out;
    private final ByteArrayOutputStream systemOutCapture = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        System.setOut(new PrintStream(systemOutCapture));
    }

    @After
    public void tearDown() {
        System.setOut(SYSTEM_OUT);
    }

    @Test
    public void parseRecognisesValidArguments() {
        ArgsForTest args = (ArgsForTest) new ArgsForTest().parse(new String[] { "--source", "mysource", "--target", "mytarget", "--package", "mypackage" });

        assertThat(args.getStatus().hasCaptured(), is(false));
        assertThat(args.getSource(), is("mysource"));
        assertThat(args.getTarget(), is("mytarget"));
        assertThat(args.getPackageName(), is("mypackage"));
    }

    @Test
    public void packageIsOptional() {
        ArgsForTest args = (ArgsForTest) new ArgsForTest().parse(new String[] { "-s", "mysource", "-t", "mytarget" });

        assertThat(args.getStatus().hasCaptured(), is(false));
        assertThat(args.getSource(), is("mysource"));
        assertThat(args.getTarget(), is("mytarget"));
        assertThat(args.getPackageName(), is(""));
    }

    @Test
    public void missingArgsCausesHelp() throws IOException {
        ArgsForTest args = (ArgsForTest) new ArgsForTest().parse(new String[] {});

        assertThat(args.getStatus().hasCaptured(), is(true));
        assertThat(args.getStatus().getValue(), is(1));
        assertThat(new String(systemOutCapture.toByteArray(), "UTF-8"), is(HELP_OUTPUT));
    }

    @Test
    public void requestingHelpCausesHelp() throws IOException {
        ArgsForTest args = (ArgsForTest) new ArgsForTest().parse(new String[] { "--help" });

        assertThat(args.getStatus().hasCaptured(), is(true));
        assertThat(new String(systemOutCapture.toByteArray(), "UTF-8"), is(HELP_OUTPUT));
    }

    private static class ArgsForTest extends Arguments {
        protected Capture<Integer> status = new Capture<Integer>();

        @Override
        protected void exit(int status) {
            this.status.setValue(status);
        }

        public Capture<Integer> getStatus() {
            return status;
        }

    }

}
