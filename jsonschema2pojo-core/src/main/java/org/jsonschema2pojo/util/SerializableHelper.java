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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JVar;
import org.jsonschema2pojo.exception.GenerationException;

public class SerializableHelper {
    private static final Comparator<AbstractJClass> INTERFACE_COMPARATOR =
            new Comparator<AbstractJClass>() {
        @Override
        public int compare(AbstractJClass object1, AbstractJClass object2) {
            if (object1 == null && object2 == null) {
                return 0;
            }
            if (object1 == null) {
                return 1;
            }
            if (object2 == null) {
                return -1;
            }
            final String name1 = object1.fullName();
            final String name2 = object2.fullName();
            if (name1 == null && name2 == null) {
                return 0;
            }
            if (name1 == null) {
                return 1;
            }
            if (name2 == null) {
                return -1;
            }
            return name1.compareTo(name2);
        }
    };


    private static void processMethodCollectionForSerializableSupport(Iterator<JMethod> methods, DataOutputStream dataOutputStream) throws IOException {
        TreeMap<String, JMethod> sortedMethods = new TreeMap<>();
        while (methods.hasNext()) {
            JMethod method = methods.next();
            //Collect non-private methods
            if ((method.mods().getValue() & JMod.PRIVATE) != JMod.PRIVATE) {
                sortedMethods.put(method.name(), method);
            }
        }
        for (JMethod method : sortedMethods.values()) {
            dataOutputStream.writeUTF(method.name());
            dataOutputStream.writeInt(method.mods().getValue());
            if (method.type() != null) {
                dataOutputStream.writeUTF(method.type().fullName());
            }
            for (JVar param : method.params()) {
                dataOutputStream.writeUTF(param.type().fullName());
            }
        }
    }

    private static void processDefinedClassForSerializableSupport(JDefinedClass jclass, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(jclass.fullName());
        dataOutputStream.writeInt(jclass.mods().getValue());

        for (JTypeVar typeParam : jclass.typeParams()) {
            dataOutputStream.writeUTF(typeParam.fullName());
        }

        //sorted
        TreeMap<String, JDefinedClass> sortedClasses = new TreeMap<>();
        Collection<JDefinedClass> classes = jclass.classes();
        for (JDefinedClass nestedClass: classes) {
            sortedClasses.put(nestedClass.fullName(), nestedClass);
        }

        for (JDefinedClass nestedClass : sortedClasses.values()) {
            processDefinedClassForSerializableSupport(nestedClass, dataOutputStream);
        }

        //sorted
        TreeSet<String> fieldNames = new TreeSet<>(jclass.fields().keySet());
        for (String fieldName : fieldNames) {
            JFieldVar fieldVar = jclass.fields().get(fieldName);
            //non private members
            if ((fieldVar.mods().getValue() & JMod.PRIVATE) != JMod.PRIVATE) {
                processFieldVarForSerializableSupport(jclass.fields().get(fieldName), dataOutputStream);
            }
        }

        Iterator<AbstractJClass> interfaces = jclass._implements();
        List<AbstractJClass> interfacesList = new ArrayList<>();
        while (interfaces.hasNext()) {
            AbstractJClass aInterface = interfaces.next();
            interfacesList.add(aInterface);
        }

        Collections.sort(interfacesList, INTERFACE_COMPARATOR);
        for (AbstractJClass aInterface : interfacesList) {
            dataOutputStream.writeUTF(aInterface.fullName());
        }

        //we should probably serialize the parent class too! (but what if it has serialversionUID on it? that would be a field and would affect the serialversionUID!)
        if (jclass._extends() != null) {
            dataOutputStream.writeUTF(jclass._extends().fullName());
        }

        processMethodCollectionForSerializableSupport(jclass.methods().iterator(), dataOutputStream);
        processMethodCollectionForSerializableSupport(jclass.constructors(), dataOutputStream);
    }


    private static void processFieldVarForSerializableSupport(JFieldVar fieldVar, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(fieldVar.name());
        dataOutputStream.writeInt(fieldVar.mods().getValue());
        AbstractJType type = fieldVar.type();
        dataOutputStream.writeUTF(type.fullName());
    }

    public static void addSerializableSupport(JDefinedClass jclass) {
        jclass._implements(Serializable.class);

        try {

            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

            processDefinedClassForSerializableSupport(jclass, dataOutputStream);

            dataOutputStream.flush();

            final MessageDigest digest = MessageDigest.getInstance("SHA");
            final byte[] digestBytes = digest.digest(byteArrayOutputStream.toByteArray());
            long serialVersionUID = 0L;

            for (int i = Math.min(digestBytes.length, 8) - 1; i >= 0; i--) {
                serialVersionUID = serialVersionUID << 8 | digestBytes[i] & 0xff;
            }

            JFieldVar  serialUIDField = jclass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, long.class, "serialVersionUID");
            serialUIDField.init(JExpr.lit(serialVersionUID));

        } catch (IOException exception) {
            throw new GenerationException("IOException while generating serialversionUID field while adding serializable support to class: " + jclass.fullName(), exception);
        } catch (NoSuchAlgorithmException exception) {
            throw new GenerationException("SHA algorithm not found when trying to generate serialversionUID field while adding serializable support to class: " + jclass.fullName(), exception);
        }
    }
}
