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

import org.jsonschema2pojo.exception.GenerationException;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;

public class TypeUtil {

    public static JClass resolveType(JClassContainer _package, String typeDefinition) {

        try {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) JavaParser.parseBodyDeclaration(typeDefinition + " foo;");
            ClassOrInterfaceType c = (ClassOrInterfaceType) fieldDeclaration.getType().getChildrenNodes().get(0);

            return buildClass(_package, c, 0);
        } catch (ParseException e) {
            throw new GenerationException(e);
        }
    }

    private static JClass buildClass(JClassContainer _package, ClassOrInterfaceType c, int arrayCount) {
        final String packagePrefix = (c.getScope() != null) ? c.getScope().toString() + "." : "";

        JClass _class;
        try {
            _class = _package.owner().ref(Thread.currentThread().getContextClassLoader().loadClass(packagePrefix + c.getName()));
        } catch (ClassNotFoundException e) {
            _class = _package.owner().ref(packagePrefix + c.getName());
        }

        for (int i=0; i<arrayCount; i++) {
            _class = _class.array();
        }

        List<Type> typeArgs = c.getTypeArgs();
        if (typeArgs != null && typeArgs.size() > 0) {
            JClass[] genericArgumentClasses = new JClass[typeArgs.size()];

            for (int i=0; i<typeArgs.size(); i++) {
                genericArgumentClasses[i] = buildClass(_package, (ClassOrInterfaceType) ((ReferenceType) typeArgs.get(i)).getType(), ((ReferenceType) typeArgs.get(i)).getArrayCount());
            }

            _class = _class.narrow(genericArgumentClasses);
        }

        return _class;
    }

}
