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

import java.util.List;

import org.jsonschema2pojo.exception.GenerationException;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.WildcardType;

public class TypeUtil {

    public static JClass resolveType(JClassContainer _package, String typeDefinition) {

        try {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) JavaParser.parseBodyDeclaration(typeDefinition + " foo;");
            ClassOrInterfaceType c = (ClassOrInterfaceType) ((ReferenceType) fieldDeclaration.getType()).getType();

            return buildClass(_package, c, 0);
        } catch (ParseException e) {
            throw new GenerationException("Couldn't parse type: " + typeDefinition, e);
        }
    }

    private static JClass buildClass(JClassContainer _package, ClassOrInterfaceType c, int arrayCount) {
        final String packagePrefix = (c.getScope() != null) ? c.getScope().toString() + "." : "";

        JClass _class = _package.owner().ref(packagePrefix + c.getName());

        for (int i = 0; i < arrayCount; i++) {
            _class = _class.array();
        }

        List<Type> typeArgs = c.getTypeArgs();
        if (typeArgs != null && typeArgs.size() > 0) {
            JClass[] genericArgumentClasses = new JClass[typeArgs.size()];

            for (int i = 0; i < typeArgs.size(); i++) {
                final Type type = typeArgs.get(i);

                final JClass resolvedClass;
                if (type instanceof WildcardType) {
                    final WildcardType wildcardType = (WildcardType) type;
                    if (wildcardType.getSuper() != null) {
                        throw new IllegalArgumentException("\"? super \" declaration is not yet supported");
                    } else if (wildcardType.getExtends() != null) {
                        resolvedClass = buildClass(_package, (ClassOrInterfaceType) wildcardType.getExtends().getType(), 0).wildcard();
                    } else {
                        resolvedClass = _package.owner().ref(Object.class).wildcard();
                    }
                } else {
                    final ReferenceType referenceType = (ReferenceType) type;
                    resolvedClass = buildClass(_package, (ClassOrInterfaceType) referenceType.getType(), referenceType.getArrayCount());
                }

                genericArgumentClasses[i] = resolvedClass;
            }

            _class = _class.narrow(genericArgumentClasses);
        }

        return _class;
    }

}
