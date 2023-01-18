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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.InputDecorator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class JsonFactoryProvider extends InputDecorator {

    private final ObjectMapper objectMapper;
    private final SchemaGenerator schemaGenerator;

    private JsonFactoryProvider(JsonFactory inputFactory, SchemaGenerator schemaGenerator) {
        objectMapper = new ObjectMapper(inputFactory)
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        this.schemaGenerator = schemaGenerator;
    }

    @Override
    public InputStream decorate(IOContext ioContext, InputStream inputStream) throws IOException {
        return new ByteArrayInputStream(schemaGenerator.schemaFromExample(objectMapper.readTree(inputStream)).toString().getBytes());
    }

    @Override
    public InputStream decorate(IOContext ioContext, byte[] src, int offset, int length) throws IOException {
        return new ByteArrayInputStream(schemaGenerator.schemaFromExample(objectMapper.readTree(src, offset, length)).toString().getBytes());
    }

    @Override
    public Reader decorate(IOContext ioContext, Reader reader) throws IOException {
        return new StringReader(schemaGenerator.schemaFromExample(objectMapper.readTree(reader)).toString());
    }

    /**
     * Get {@code JsonFactory} for handling input of given source type
     * backed by default {@link SchemaGenerator}
     *
     * @param sourceType input's source type
     * @return {@code} {@link JsonFactory} capable of resolving schema for given source type
     */
    public static JsonFactory ofSourceType(SourceType sourceType) {
        return ofSourceTypeAndSchemaGenerator(sourceType, new SchemaGenerator());
    }

    /**
     * Get {@code JsonFactory} for handling input of given source type
     * backed by provided {@link SchemaGenerator}
     *
     * @param sourceType input's source type
     * @param schemaGenerator schema generator to use as
     * @return {@code} {@link JsonFactory} capable of resolving schema for given source type
     */
    public static JsonFactory ofSourceTypeAndSchemaGenerator(SourceType sourceType, SchemaGenerator schemaGenerator) {
        if (sourceType == SourceType.YAMLSCHEMA) {
            return new YAMLFactory();
        } else if (sourceType == SourceType.YAML) {
            return new JsonFactoryBuilder().inputDecorator(new JsonFactoryProvider(new YAMLFactory(), schemaGenerator)).build();
        } else if (sourceType == SourceType.JSON) {
            return new JsonFactoryBuilder().inputDecorator(new JsonFactoryProvider(null, schemaGenerator)).build();
        } else {
            return null;
        }
    }

}
