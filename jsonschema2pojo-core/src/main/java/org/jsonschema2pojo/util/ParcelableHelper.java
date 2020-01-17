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

import com.sun.codemodel.*;
import static org.jsonschema2pojo.util.Models.*;
import static org.apache.commons.lang3.StringUtils.*;

public class ParcelableHelper {

    public void addWriteToParcel(JDefinedClass jclass) {
        JMethod method = jclass.method(JMod.PUBLIC, void.class, "writeToParcel");
        JVar dest = method.param(jclass.owner().directClass("android.os.Parcel"), "dest");
        method.param(int.class, "flags");

        // Call super.writeToParcel
        if (extendsParcelable(jclass)) {
            method.body().directStatement("super.writeToParcel(dest, flags);");
        }
        for (JFieldVar f : jclass.fields().values()) {
            if( (f.mods().getValue() & JMod.STATIC) == JMod.STATIC ) {
                continue;
            }
            if (f.type().erasure().name().equals("List")) {
                method.body().invoke(dest, "writeList").arg(f);
            } else {
                method.body().invoke(dest, "writeValue").arg(f);
            }
        }
    }
    
    public void addDescribeContents(JDefinedClass jclass) {
        JMethod method = jclass.method(JMod.PUBLIC, int.class, "describeContents");
        method.body()._return(JExpr.lit(0));
    }
    
    public void addCreator(JDefinedClass jclass) {
        JClass creatorType = jclass.owner().directClass("android.os.Parcelable.Creator").narrow(jclass);
        JDefinedClass creatorClass = jclass.owner().anonymousClass(creatorType);
        
        addCreateFromParcel(jclass, creatorClass);
        addNewArray(jclass, creatorClass);
        
        JFieldVar creatorField = jclass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, creatorType, "CREATOR");
        creatorField.init(JExpr._new(creatorClass));
    }

    public void addConstructorFromParcel(JDefinedClass jclass) {
        JMethod ctorFromParcel = jclass.constructor(JMod.PROTECTED);
        JVar in = ctorFromParcel.param(jclass.owner().directClass("android.os.Parcel"), "in");

        if (extendsParcelable(jclass)) {
            ctorFromParcel.body().directStatement("super(in);");
        }
        for (JFieldVar f : jclass.fields().values()) {
            if( (f.mods().getValue() & JMod.STATIC) == JMod.STATIC ) {
                continue;
            }
            if (f.type().erasure().name().equals("List")) {
                ctorFromParcel.body()
                        .invoke(in, "readList")
                        .arg(JExpr._this().ref(f))
                        .arg(JExpr.direct(getListType(f.type()) + ".class.getClassLoader()"));
             } else {
                ctorFromParcel.body().assign(
                        JExpr._this().ref(f),
                        JExpr.cast(
                                f.type(),
                                in.invoke("readValue").arg(JExpr.direct(f.type().erasure().name() + ".class.getClassLoader()"))
                        )
                );
            }

        }
    }


    private void addNewArray(JDefinedClass jclass, JDefinedClass creatorClass) {
        JMethod newArray = creatorClass.method(JMod.PUBLIC, jclass.array(), "newArray");
        newArray.param(int.class, "size");
        newArray.body()._return(JExpr.direct("new " + jclass.name() + "[size]"));
    }

    private void addCreateFromParcel(JDefinedClass jclass, JDefinedClass creatorClass) {
        JMethod createFromParcel = creatorClass.method(JMod.PUBLIC, jclass, "createFromParcel");
        JVar in = createFromParcel.param(jclass.owner().directClass("android.os.Parcel"), "in");
        suppressWarnings(createFromParcel, "unchecked");
        createFromParcel.body()._return(JExpr._new(jclass).arg(in));
    }

    private boolean extendsParcelable(final JDefinedClass jclass) {
        final java.util.Iterator<JClass> interfaces = jclass._extends() != null ? jclass._extends()._implements() : null;
        if (interfaces != null) {
            while (interfaces.hasNext()) {
                final JClass iface = interfaces.next();
                if (iface.erasure().name().equals("Parcelable")) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getListType(JType jType) {
        final String typeName = jType.fullName();
        return substringBeforeLast(substringAfter(typeName, "<"), ">");
    }

}
