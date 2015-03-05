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

package org.jsonschema2pojo.maven;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.SourceType;

public class Source implements GenerationConfig {
    /**
     * Most configuration happens on the Mojo.  This allows the object to be used by itself without managing all attributes.
     */
    private GenerationConfig parentConfig;

    /**
     * The target package for the given source paths.
     */
    private String targetPackage;

    /**
     * List of source paths to generate source for.
     */
    private List<File> sourcePaths;

    /**
     * Implemented as an "add" because Mojo will call it for each sourcePath element in the plugin configuration.
     * @param sourcePath
     */
    public void setSourcePath(File sourcePath) {
        if (sourcePaths == null) {
            sourcePaths = new LinkedList<File>();
        }
        sourcePaths.add(sourcePath);
    }

    public List<File> getSourcePaths() {
        return sourcePaths;
    }

    public void setSourcePaths(List<File> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /**
     * Before using this object as GenerationConfig, another "parent" object (Jsonschema2PojoMojo) needs to be set.
     * @param parentConfig
     */
    public void setParentConfig(GenerationConfig parentConfig) {
        this.parentConfig = parentConfig;
    }

    public GenerationConfig getParentConfig() {
        return parentConfig;
    }

    @Override
    public AnnotationStyle getAnnotationStyle() {
        return parentConfig.getAnnotationStyle();
    }

    @Override
    public Class<? extends Annotator> getCustomAnnotator() {
        return parentConfig.getCustomAnnotator();
    }

    @Override
    public String getOutputEncoding() {
        return parentConfig.getOutputEncoding();
    }

    @Override
    public char[] getPropertyWordDelimiters() {
        return parentConfig.getPropertyWordDelimiters();
    }

    @Override
    public Iterator<File> getSource() {
        return this.sourcePaths.iterator();
    }

    @Override
    public SourceType getSourceType() {
        return parentConfig.getSourceType();
    }

    @Override
    public File getTargetDirectory() {
        return parentConfig.getTargetDirectory();
    }

    /**
     * If target package is not set locally, use the value from the parent config.
     */
    @Override
    public String getTargetPackage() {
        if (this.targetPackage != null) {
            return this.targetPackage;
        } else {
            return parentConfig.getTargetPackage();
        }
    }

    @Override
    public boolean isGenerateBuilders() {
        return parentConfig.isGenerateBuilders();
    }

    @Override
    public boolean isIncludeHashcodeAndEquals() {
        return parentConfig.isIncludeHashcodeAndEquals();
    }

    @Override
    public boolean isIncludeJsr303Annotations() {
        return parentConfig.isIncludeJsr303Annotations();
    }

    @Override
    public boolean isIncludeToString() {
        return parentConfig.isIncludeToString();
    }

    @Override
    public boolean isRemoveOldOutput() {
        return parentConfig.isRemoveOldOutput();
    }

    @Override
    public boolean isUseJodaDates() {
        return parentConfig.isUseJodaDates();
    }

    @Override
    public boolean isUseLongIntegers() {
        return parentConfig.isUseLongIntegers();
    }

    @Override
    public boolean isUsePrimitives() {
        return parentConfig.isUsePrimitives();
    }
}