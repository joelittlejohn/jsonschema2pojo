/**
 * Copyright ¬© 2010-2014 Nokia
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

public enum URLProtocol {
    FILE("file"),
    RESOURCE("resource"),
    JAVA("java"),
    CLASSPATH("classpath"),
    HTTP("http"),
    HTTPS("https"),
    NO_PROTOCOL("");

    private String protocol;

    URLProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public static URLProtocol fromString(final String input) {
        for (URLProtocol protocol : URLProtocol.values()) {
            if (protocol.getProtocol().equalsIgnoreCase(input)) {
                return protocol;
            }
        }
        // default to file
        return NO_PROTOCOL;
    }
}
