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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

@SuppressWarnings("static-access")
public class Arguments {

    private static final int EXIT_OKAY = 0;
    private static final int EXIT_ERROR = 1;

    private static Options options = new Options();
    static {
        options.addOption("h", "help", false, "Print help information and exit");
        options.addOption(OptionBuilder.hasArg().isRequired(false).withDescription("A java package used for generated types").withLongOpt("package").withArgName("package name").create("p"));
        options.addOption(OptionBuilder.hasArg().isRequired().withDescription("The target directory into which generated types will be written").withLongOpt("target").withArgName("directory").create("t"));
        options.addOption(OptionBuilder.hasArg().isRequired().withDescription("The source file or directory from which JSON Schema will be read").withLongOpt("source").create("s"));
        options.addOption(OptionBuilder.hasArg(false).isRequired(false).withDescription("Generate builder-style methods as well as setters").withLongOpt("generate-builders").create("b"));
    }

    private String source;
    private String target;
    private String packageName;
    private Map<String, String> behaviourProperties;

    public Arguments parse(String[] args) {

        try {
            CommandLine commandLine = new PosixParser().parse(options, args);

            if (commandLine.hasOption("help")) {
                printHelp(EXIT_OKAY);
            }

            this.source = commandLine.getOptionValue("source");
            this.packageName = defaultString(commandLine.getOptionValue("package"));
            this.target = commandLine.getOptionValue("target");

            this.behaviourProperties = new HashMap<String, String>();

        } catch (ParseException e) {
            printHelp(EXIT_ERROR);
        }

        return this;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    private void printHelp(int status) {
        new HelpFormatter().printHelp("generate", options, true);
        exit(status);
    }

    protected void exit(int status) {
        System.exit(status);
    }

    public Map<String, String> getBehaviourProperties() {
        return behaviourProperties;
    }

}
