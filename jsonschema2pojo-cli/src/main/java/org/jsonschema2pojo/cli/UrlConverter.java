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

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;
import org.jsonschema2pojo.util.URLUtil;

import java.net.URL;

import static org.apache.commons.lang3.StringUtils.isBlank;


/**
 * Convert a string into a url.
 *
 * @author jvasiljevich
 */
public class UrlConverter extends BaseConverter<URL> {

    public UrlConverter(String optionName) {
        super(optionName);
    }

    public URL convert(String value) {
        if (isBlank(value)) {
            throw new ParameterException(getErrorString("a blank value", "a valid URL"));
        }

        try {
            return URLUtil.parseURL(value);
        } catch (IllegalArgumentException e) {
            throw new ParameterException(getErrorString(value, "a valid URL"));
        }

    }
}
