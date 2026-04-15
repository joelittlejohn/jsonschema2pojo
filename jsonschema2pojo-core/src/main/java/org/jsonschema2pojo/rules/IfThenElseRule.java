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

package org.jsonschema2pojo.rules;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;
import org.jsonschema2pojo.util.AnnotationHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

/**
 * Handles if/then/else conditional schemas by detecting discriminated unions.
 * When the "if" block checks a property against a const value and "then"
 * provides additional properties (often via $ref), this rule generates
 * subclasses with Jackson @JsonSubTypes annotations.
 */
public class IfThenElseRule implements Rule<JDefinedClass, JDefinedClass> {

    private final RuleFactory ruleFactory;

    protected IfThenElseRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass jclass, Schema schema) {
        if (!node.has("if") || !node.has("then")) {
            return jclass;
        }

        DiscriminatorInfo info = extractDiscriminator(node.get("if"));
        if (info == null) {
            return jclass;
        }

        ResolvedThen resolved = resolveThen(node.get("then"), schema);
        if (resolved == null) {
            return jclass;
        }

        String subclassName = deriveSubclassName(resolved.content, info.constValue, null);
        if (subclassName == null) {
            return jclass;
        }

        try {
            JPackage pkg = jclass.getPackage();
            String uniqueName = ruleFactory.getNameHelper().getUniqueClassName(subclassName, resolved.content, pkg);

            JDefinedClass subclass;
            try {
                subclass = pkg._class(JMod.PUBLIC, uniqueName);
            } catch (JClassAlreadyExistsException e) {
                return jclass;
            }

            subclass._extends(jclass);

            if (ruleFactory.getGenerationConfig().isIncludeGeneratedAnnotation()) {
                AnnotationHelper.addGeneratedAnnotation(ruleFactory.getGenerationConfig(), subclass);
            }

            applyPropertiesToSubclass(subclassName, resolved.content, resolved.schema, subclass);

            registerSubtype(jclass, subclass, info.propertyName, info.constValue);

        } catch (Exception e) {
            ruleFactory.getLogger().error("Failed to generate subclass for if/then: " + subclassName, e);
        }

        return jclass;
    }

    /**
     * Applies this rule to all if/then blocks in a parent allOf array,
     * and adds @JsonTypeInfo + @JsonSubTypes to the base class.
     */
    public JDefinedClass applyAll(String nodeName, JsonNode allOfNode, JsonNode parent, JDefinedClass jclass, Schema schema) {
        if (allOfNode == null || !allOfNode.isArray()) {
            return jclass;
        }

        String discriminatorProp = null;
        List<SubtypeEntry> subtypes = new ArrayList<>();

        int allOfIndex = 0;
        for (JsonNode entry : allOfNode) {
            if (entry.has("if") && entry.has("then")) {
                DiscriminatorInfo info = extractDiscriminator(entry.get("if"));
                if (info != null) {
                    if (discriminatorProp == null) {
                        discriminatorProp = info.propertyName;
                    }

                    ResolvedThen resolved = resolveThenWithIndex(entry.get("then"), schema, allOfIndex);
                    if (resolved == null) { allOfIndex++; continue; }

                    String subclassName = deriveSubclassName(resolved.content, info.constValue, resolved.refName);
                    if (subclassName == null) { allOfIndex++; continue; }

                    JPackage pkg = jclass.getPackage();
                    String uniqueName;
                    if (resolved.refName != null) {
                        uniqueName = ruleFactory.getNameHelper().getClassName(subclassName, null, pkg);
                        uniqueName = ruleFactory.getNameHelper().getUniqueClassName(uniqueName, null, pkg);
                    } else {
                        uniqueName = ruleFactory.getNameHelper().getUniqueClassName(subclassName, resolved.content, pkg);
                    }

                    try {
                        JDefinedClass subclass = pkg._class(JMod.PUBLIC, uniqueName);
                        subclass._extends(jclass);

                        if (ruleFactory.getGenerationConfig().isIncludeGeneratedAnnotation()) {
                            AnnotationHelper.addGeneratedAnnotation(ruleFactory.getGenerationConfig(), subclass);
                        }

                        try {
                            applyPropertiesToSubclass(subclassName, resolved.content, resolved.schema, subclass);
                        } catch (IllegalArgumentException e) {
                            ruleFactory.getLogger().error("Property resolution failed for subclass " + subclassName
                                    + " (allOfIndex=" + allOfIndex + ", schema=" + resolved.schema.getId()
                                    + "): " + e.getMessage(), e);
                        }

                        ruleFactory.getAnnotator().propertyInclusion(subclass, resolved.content);

                        subtypes.add(new SubtypeEntry(info.constValue, subclass));
                    } catch (JClassAlreadyExistsException e) {
                        // skip duplicates
                    }
                }
            }
            allOfIndex++;
        }

        if (discriminatorProp != null && !subtypes.isEmpty()) {
            addJsonTypeAnnotations(jclass, discriminatorProp, subtypes);
        }

        return jclass;
    }

    private void applyPropertiesToSubclass(String subclassName, JsonNode thenContent, Schema thenSchema, JDefinedClass subclass) {
        if (thenContent.has("properties")) {
            JsonNode props = thenContent.get("properties");
            for (Iterator<Map.Entry<String, JsonNode>> fields = props.fields(); fields.hasNext(); ) {
                Map.Entry<String, JsonNode> field = fields.next();
                String propName = ruleFactory.getNameHelper().getPropertyName(field.getKey(), field.getValue());
                if (parentHasField(subclass, propName)) {
                    continue;
                }
                ruleFactory.getPropertyRule().apply(field.getKey(), field.getValue(), props, subclass, thenSchema);
            }
        }

        if (thenContent.has("required")) {
            ruleFactory.getRequiredArrayRule().apply(subclassName, thenContent.get("required"), thenContent, subclass, thenSchema);
        }

        // Constructors for subclasses are not generated to avoid conflicts
        // with inherited constructors from the parent class
    }

    private void addJsonTypeAnnotations(JDefinedClass jclass, String discriminatorProp, List<SubtypeEntry> subtypes) {
        try {
            Class<?> jsonTypeInfoClass = Class.forName("com.fasterxml.jackson.annotation.JsonTypeInfo");
            Class<?> jsonSubTypesClass = Class.forName("com.fasterxml.jackson.annotation.JsonSubTypes");

            JAnnotationUse typeInfo = jclass.annotate(jclass.owner().ref(jsonTypeInfoClass));
            typeInfo.param("use", jclass.owner().ref(
                    Class.forName("com.fasterxml.jackson.annotation.JsonTypeInfo$Id")).staticRef("NAME"));
            typeInfo.param("include", jclass.owner().ref(
                    Class.forName("com.fasterxml.jackson.annotation.JsonTypeInfo$As")).staticRef("EXISTING_PROPERTY"));
            typeInfo.param("property", discriminatorProp);
            typeInfo.param("visible", true);

            JAnnotationUse subTypesAnnotation = jclass.annotate(jclass.owner().ref(jsonSubTypesClass));
            JAnnotationArrayMember valueArray = subTypesAnnotation.paramArray("value");

            for (SubtypeEntry entry : subtypes) {
                JAnnotationUse typeEntry = valueArray.annotate(
                        jclass.owner().ref(Class.forName("com.fasterxml.jackson.annotation.JsonSubTypes$Type")));
                typeEntry.param("value", entry.subclass);
                typeEntry.param("name", entry.discriminatorValue);
            }
        } catch (ClassNotFoundException e) {
            ruleFactory.getLogger().error("Jackson annotations not on classpath, cannot add @JsonTypeInfo/@JsonSubTypes", e);
        }
    }

    private void registerSubtype(JDefinedClass baseClass, JDefinedClass subclass, String propertyName, String constValue) {
        // Individual registration - used when called one-at-a-time
        // The bulk annotation is handled by applyAll
    }

    private DiscriminatorInfo extractDiscriminator(JsonNode ifNode) {
        JsonNode properties = ifNode.get("properties");
        if (properties == null) return null;

        Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
        if (!fields.hasNext()) return null;

        Map.Entry<String, JsonNode> field = fields.next();
        String propertyName = field.getKey();
        JsonNode propSchema = field.getValue();

        String constValue = null;
        if (propSchema.has("const")) {
            constValue = propSchema.get("const").asText();
        } else if (propSchema.has("enum") && propSchema.get("enum").size() == 1) {
            constValue = propSchema.get("enum").get(0).asText();
        }

        if (constValue == null) return null;
        return new DiscriminatorInfo(propertyName, constValue);
    }

    private ResolvedThen resolveThen(JsonNode thenNode, Schema parentSchema) {
        if (thenNode.has("$ref")) {
            String ref = thenNode.get("$ref").asText();
            Schema refSchema = ruleFactory.getSchemaStore().create(
                    parentSchema, ref,
                    ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
            return new ResolvedThen(refSchema.getContent(), refSchema, extractNameFromRef(ref));
        }
        return new ResolvedThen(thenNode, parentSchema, null);
    }

    /**
     * Resolves a then node, using its actual JSON pointer path within the
     * document when it's an inline schema (not a $ref). This ensures
     * PropertyRule can correctly resolve property paths.
     */
    private ResolvedThen resolveThenWithIndex(JsonNode thenNode, Schema parentSchema, int allOfIndex) {
        if (thenNode.has("$ref")) {
            String ref = thenNode.get("$ref").asText();
            String refName = extractNameFromRef(ref);
            Schema refSchema = ruleFactory.getSchemaStore().create(
                    parentSchema, ref,
                    ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
            return new ResolvedThen(refSchema.getContent(), refSchema, refName);
        }

        String parentFragment = parentSchema.getId() != null ? parentSchema.getId().getFragment() : null;
        String thenPath;
        if (parentFragment != null) {
            thenPath = "#" + parentFragment + "/allOf/" + allOfIndex + "/then";
        } else {
            thenPath = "#/allOf/" + allOfIndex + "/then";
        }

        Schema thenSchema = ruleFactory.getSchemaStore().create(
                parentSchema, thenPath,
                ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
        return new ResolvedThen(thenSchema.getContent(), thenSchema, null);
    }

    private String extractNameFromRef(String ref) {
        if (ref == null || ref.isEmpty()) return null;
        String[] parts = ref.split("[/\\\\#]");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }

    private static class ResolvedThen {
        final JsonNode content;
        final Schema schema;
        final String refName;

        ResolvedThen(JsonNode content, Schema schema, String refName) {
            this.content = content;
            this.schema = schema;
            this.refName = refName;
        }
    }

    private String deriveSubclassName(JsonNode thenNode, String constValue, String refName) {
        if (refName != null && !refName.isEmpty()) {
            return refName;
        }
        if (thenNode.has("title")) {
            return thenNode.get("title").asText();
        }
        if (constValue != null && !constValue.isEmpty()) {
            String name = constValue.substring(0, 1).toUpperCase() + constValue.substring(1);
            name = name.replaceAll("[^A-Za-z0-9_]", "");
            if (isReservedJavaName(name)) {
                name = name + "Type";
            }
            return name;
        }
        return null;
    }

    private boolean isReservedJavaName(String name) {
        switch (name) {
            case "String":
            case "Integer":
            case "Long":
            case "Double":
            case "Float":
            case "Boolean":
            case "Byte":
            case "Short":
            case "Character":
            case "Object":
            case "Class":
            case "Number":
            case "Array":
            case "Date":
            case "List":
            case "Map":
            case "Set":
            case "Void":
                return true;
            default:
                return false;
        }
    }

    private boolean parentHasField(JDefinedClass subclass, String fieldName) {
        com.sun.codemodel.JClass parent = subclass._extends();
        if (parent instanceof JDefinedClass) {
            JDefinedClass parentClass = (JDefinedClass) parent;
            return parentClass.fields().containsKey(fieldName);
        }
        return false;
    }

    private static class DiscriminatorInfo {
        final String propertyName;
        final String constValue;

        DiscriminatorInfo(String propertyName, String constValue) {
            this.propertyName = propertyName;
            this.constValue = constValue;
        }
    }

    private static class SubtypeEntry {
        final String discriminatorValue;
        final JDefinedClass subclass;

        SubtypeEntry(String discriminatorValue, JDefinedClass subclass) {
            this.discriminatorValue = discriminatorValue;
            this.subclass = subclass;
        }
    }
}
