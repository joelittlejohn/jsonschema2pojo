/**
 * Copyright © 2010-2020 Nokia
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

import static org.apache.commons.lang3.StringUtils.*;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;

/**
 * A converter that can create a class given a fully qualified class name. Type
 * parameters for the class are omitted, since they are no use to JCommander at
 * runtime (and the wild-cards prove problematic when attaching this converter
 * to an option).
 */
@SuppressWarnings("rawtypes")
public class ClassConverter extends BaseConverter<Class> {

    /**
     * Create a new class converter.
     * 
     * @param optionName
     *            The name of the option that will be using this converter.
     */
    public ClassConverter(String optionName) {
        super(optionName);
    }

    @Override
    public Class convert(String value) {

        if (isBlank(value)) {
            throw new ParameterException(getErrorString("a blank value", "a class"));
        }

        try {
            return Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new ParameterException(getErrorString(value, "a class"));
        }
    }

}
