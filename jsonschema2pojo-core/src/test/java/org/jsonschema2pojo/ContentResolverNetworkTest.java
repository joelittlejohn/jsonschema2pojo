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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
  * @author <a href="https://github.com/s13o">s13o</a>
  * @since 3/17/2017
  */
public class ContentResolverNetworkTest {

    private static final String ADDRESS = "localhost";

    @RegisterExtension
    private static final WireMockExtension server = WireMockExtension.newInstance()
            .options(options().dynamicPort().bindAddress(ADDRESS).usingFilesUnderClasspath("wiremock"))
            .build();

    private final ContentResolver resolver = new ContentResolver();
    
    @Test
    public void brokenLinkCausesIllegalArgumentException() {
        URI brokenHttpUri = URI.create("http://" + ADDRESS + ":" + server.getPort() + "/address404.json");
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(brokenHttpUri));
    }
    
    @Test
    public void serverErrorCausesIllegalArgumentException() {
        URI brokenHttpUri = URI.create("http://" + ADDRESS + ":" + server.getPort() + "/address500.json");
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(brokenHttpUri));
    }

    @Test
    public void httpLinkIsResolvedToContent() {
        URI httpUri = URI.create("http://" + ADDRESS + ":" + server.getPort() + "/address.json");
        JsonNode uriContent = resolver.resolve(httpUri);
        assertThat(uriContent.path("description").asText().length(), is(greaterThan(0)));
    }

}
