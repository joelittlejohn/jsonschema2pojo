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

package org.jsonschema2pojo;

import java.util.LinkedHashSet;

import com.sun.codemodel.JFieldVar;

import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Annotates generated Java types using the Jackson 3.x mapping annotations (version 2.x of jackson-annotations
 * are still used in Jackson 3.x, but annotations from jackson-databind are different)
 *
 * @see <a
 *      href="https://github.com/FasterXML/jackson-annotations">https://github.com/FasterXML/jackson-annotations</a>
 * @see <a
 *      href="https://github.com/FasterXML/jackson-databind/tree/3.x">https://github.com/FasterXML/jackson-databind (3.x)</a>
 */
public class Jackson3Annotator extends JacksonAnnotator {

    public Jackson3Annotator(GenerationConfig generationConfig) {
        super(generationConfig);
    }

    @Override
    protected void addJsonDeserializeAnnotation(JFieldVar field) {
        field.annotate(JsonDeserialize.class).param("as", LinkedHashSet.class);
    }
}
