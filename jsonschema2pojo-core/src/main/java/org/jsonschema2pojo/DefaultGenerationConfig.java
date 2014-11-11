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

import org.jsonschema2pojo.rules.RuleFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

/**
 * A generation config that returns default values for all behavioural options.
 */
public class DefaultGenerationConfig implements GenerationConfig {

    /**
     * @return <code>false</code>
     */
    @Override
    public boolean isGenerateBuilders() {
        return false;
    }

    /**
     * @return <code>false</code>
     */
    @Override
    public boolean isUsePrimitives() {
        return false;
    }

    /**
     * Unsupported since no default source is possible.
     */
    @Override
    public Iterator<File> getSource() {
        throw new UnsupportedOperationException("No default source available");
    }

    /**
     * @return the current working directory
     */
    @Override
    public File getTargetDirectory() {
        return new File(".");
    }

    /**
     * @return the 'default' package (i.e. an empty string)
     */
    @Override
    public String getTargetPackage() {
        return "";
    }

    /**
     * @return an empty array (i.e. no word delimiters)
     */
    @Override
    public char[] getPropertyWordDelimiters() {
        return new char[] { '-', ' ', '_' };
    }

    /**
     * @return <code>false</code>
     */
    @Override
    public boolean isUseLongIntegers() {
        return false;
    }

    @Override
    public boolean isUseDoubleNumbers() {
        return true;
    }

    /**
     * @return <code>true</code>
     */
    @Override
    public boolean isIncludeHashcodeAndEquals() {
        return true;
    }

    /**
     * @return <code>true</code>
     */
    @Override
    public boolean isIncludeToString() {
        return true;
    }

    /**
     * @return {@link AnnotationStyle#JACKSON2}
     */
    @Override
    public AnnotationStyle getAnnotationStyle() {
        return AnnotationStyle.JACKSON;
    }

    /**
     * {@link NoopAnnotator}
     */
    @Override
    public Class<? extends Annotator> getCustomAnnotator() {
        return NoopAnnotator.class;
    }

    @Override
    public Class<? extends RuleFactory> getCustomRuleFactory() {
        return RuleFactory.class;
    }

    /**
     * @return <code>false</code>
     */
    @Override
    public boolean isIncludeJsr303Annotations() {
        return false;
    }

    /**
     * @return {@link SourceType#JSONSCHEMA}
     */
    @Override
    public SourceType getSourceType() {
        return SourceType.JSONSCHEMA;
    }

    /**
     * @return UTF-8
     */
    @Override
    public String getOutputEncoding() {
        return "UTF-8";
    }

    /**
     * @return false
     */
    @Override
    public boolean isRemoveOldOutput() {
        return false;
    }

    /**
     * @return false
     */
    @Override
    public boolean isUseJodaDates() {
        return false;
    }

    @Override
    public boolean isUseCommonsLang3() {
        return false;
    }

    @Override
    public FileFilter getFileFilter() {
        return new AllFileFilter();
    }

    /**
     * @return <code>true</code>
     */
    @Override
    public boolean isInitializeCollections() {
        return true;
    }

    @Override
    public String getClassNamePrefix() {
        return "";
    }

    @Override
    public String getClassNameSuffix() {
        return "";
    }
}