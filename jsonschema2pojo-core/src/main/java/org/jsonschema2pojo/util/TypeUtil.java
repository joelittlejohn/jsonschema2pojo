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

package org.jsonschema2pojo.util;

import org.jsonschema2pojo.exception.GenerationException;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.WildcardType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;

public class TypeUtil {

    public static JClass resolveType(JClassContainer _package, String typeDefinition) {

        try {
            final Type parsedType = StaticJavaParser.parseType(typeDefinition);

            if (parsedType.getElementType().isPrimitiveType()) {
                return (JClass) _package.owner().parseType(typeDefinition);
            }

            return buildClass(_package, parsedType.getElementType().asClassOrInterfaceType(), parsedType.getArrayLevel());
        } catch (ParseProblemException | ClassNotFoundException e) {
            throw new GenerationException("Couldn't parse type: " + typeDefinition, e);
        }
    }

    private static JClass buildClass(JClassContainer _package, ClassOrInterfaceType c, int arrayCount) {
        final String packagePrefix = c.getScope().map(s -> s.asString().concat(".")).orElse("");

        JClass _class = _package.owner().ref(packagePrefix + c.getName());

        for (int i = 0; i < arrayCount; i++) {
            _class = _class.array();
        }

        NodeList<Type> typeArgs = c.getTypeArguments().orElse(NodeList.nodeList());
        if (!typeArgs.isEmpty()) {
            JClass[] genericArgumentClasses = new JClass[typeArgs.size()];

            for (int i = 0; i < typeArgs.size(); i++) {
                final Type type = typeArgs.get(i);

                final JClass resolvedClass;
                if (type.isWildcardType()) {
                    final WildcardType wildcardType = type.asWildcardType();
                    if (wildcardType.getSuperType().isPresent()) {
                        // Neither JTypeWildcard nor JCodeModel.TypeNameParser::parseTypeName support '? super'
                        throw new IllegalArgumentException("\"? super \" declaration is not yet supported");
                    } else if (wildcardType.getExtendedType().isPresent()) {
                        resolvedClass = buildClass(_package, wildcardType.getExtendedType().get().asClassOrInterfaceType(), 0).wildcard();
                    } else {
                        resolvedClass = _package.owner().ref(Object.class).wildcard();
                    }
                } else {
                    final ReferenceType referenceType = type.asReferenceType();
                    resolvedClass = buildClass(
                            _package,
                            referenceType.getElementType().asClassOrInterfaceType(),
                            referenceType.getArrayLevel());
                }

                genericArgumentClasses[i] = resolvedClass;
            }

            _class = _class.narrow(genericArgumentClasses);
        }

        return _class;
    }

}
