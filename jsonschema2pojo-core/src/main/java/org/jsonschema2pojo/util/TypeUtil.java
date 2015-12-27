/**
 * Copyright © 2010-2014 Nokia
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

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.exception.ClassAlreadyExistsException;
import org.jsonschema2pojo.exception.GenerationException;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JPackage;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.jsonschema2pojo.rules.PrimitiveTypes.isPrimitive;
import static org.jsonschema2pojo.rules.PrimitiveTypes.primitiveType;

public class TypeUtil {

    public static JType resolveType(JCodeModel owner, NameHelper nameHelper, String typeDefinition)
            throws ClassNotFoundException {

        if (isPrimitive(typeDefinition, owner)) {
            return primitiveType(typeDefinition, owner);
        }

        try {
            String className = nameHelper.getJavaTypeClassName(typeDefinition);
            String baseClassName = substringBefore(className, "<");
            owner.ref(Thread.currentThread().getContextClassLoader().loadClass(baseClassName));
            return TypeUtil.resolveType(owner, className);
        } catch (ClassNotFoundException e) {
            owner.ref(Thread.currentThread().getContextClassLoader().loadClass(substringBefore(typeDefinition, "<")));
            return TypeUtil.resolveType(owner, typeDefinition);
        }
    }

    public static JClass resolveType(JCodeModel owner, String typeDefinition) {
        try {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) JavaParser.parseBodyDeclaration(typeDefinition + " foo;");
            ClassOrInterfaceType c = (ClassOrInterfaceType) fieldDeclaration.getType().getChildrenNodes().get(0);

            return buildClass(owner, c, 0);
        } catch (ParseException e) {
            throw new GenerationException(e);
        }
    }

    private static JClass buildClass(JCodeModel owner, ClassOrInterfaceType c, int arrayCount) {
        final String packagePrefix = (c.getScope() != null) ? c.getScope().toString() + "." : "";

        JClass _class;
        try {
            _class = owner.ref(Thread.currentThread().getContextClassLoader().loadClass(packagePrefix + c.getName()));
        } catch (ClassNotFoundException e) {
            _class = owner.ref(packagePrefix + c.getName());
        }

        for (int i=0; i<arrayCount; i++) {
            _class = _class.array();
        }
        
        List<Type> typeArgs = c.getTypeArgs();
        if (typeArgs != null && typeArgs.size() > 0) {
            JClass[] genericArgumentClasses = new JClass[typeArgs.size()];

            for (int i=0; i<typeArgs.size(); i++) {
                genericArgumentClasses[i] = buildClass(owner, (ClassOrInterfaceType) ((ReferenceType) typeArgs.get(i))
                        .getType(), ((ReferenceType) typeArgs.get(i)).getArrayCount());
            }
            
            _class = _class.narrow(genericArgumentClasses);
        }
        
        return _class;
    }

}
