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

/**
 * The style of annotations to be used on generated java types (to allow them to
 * support whatever kind of binding to JSON is required). Each JSON
 * parser/mapper library will have its own set of mapping annotations.
 */
public enum AnnotationStyle {

    /**
     * Jackson 2.x (alias of {@link #JACKSON2})
     * 
     * @see <a
     *      href="https://github.com/FasterXML/jackson-annotations">https://github.com/FasterXML/jackson-annotations</a>
     */
    JACKSON,

    /**
     * Jackson 1.x
     * 
     * @see <a
     *      href="http://jackson.codehaus.org/">http://jackson.codehaus.org/</a>
     */
    JACKSON1,

    /**
     * Jackson 2.x
     * 
     * @see <a
     *      href="https://github.com/FasterXML/jackson-annotations">https://github.com/FasterXML/jackson-annotations</a>
     */
    JACKSON2,

    /**
     * Gson 2.x
     */
    GSON,

    /**
     * Moshi 1.x
     *
     * @see <a
     *      href="https://github.com/square/moshi">https://github.com/square/moshi</a>
     */
    MOSHI1,

    /**
     * No-op style, adds no annotations at all.
     */
    NONE,
}
