/**
 * Copyright © 2010-2013 Nokia
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

import android.os.Parcel;
import android.os.Parcelable.Creator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class ParcelableHelper {

    public void addWriteToParcel(JDefinedClass jclass) {
        JMethod method = jclass.method(JMod.PUBLIC, void.class, "writeToParcel");
        JVar dest = method.param(Parcel.class, "dest");
        method.param(int.class, "flags");
        
        for (JFieldVar f : jclass.fields().values()) {
            method.body().invoke(dest, "writeValue").arg(f);
        }
    }
    
    public void addDescribeContents(JDefinedClass jclass) {
        JMethod method = jclass.method(JMod.PUBLIC, int.class, "describeContents");
        method.body()._return(JExpr.lit(0));
    }
    
    public void addCreator(JDefinedClass jclass) {
        JClass creatorType = jclass.owner().ref(Creator.class).narrow(jclass); 
        JDefinedClass creatorClass = jclass.owner().anonymousClass(creatorType);
        
        addCreateFromParcel(jclass, creatorClass);
        addNewArray(jclass, creatorClass);
        
        JFieldVar creatorField = jclass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, creatorType, "CREATOR");
        creatorField.init(JExpr._new(creatorClass));
    }

    private void addNewArray(JDefinedClass jclass, JDefinedClass creatorClass) {
        JMethod newArray = creatorClass.method(JMod.PUBLIC, jclass.array(), "newArray");
        newArray.param(int.class, "size");
        newArray.body()._return(JExpr.direct("new " + jclass.name() + "[size]"));
    }

    private void addCreateFromParcel(JDefinedClass jclass, JDefinedClass creatorClass) {
        JMethod createFromParcel = creatorClass.method(JMod.PUBLIC, jclass, "createFromParcel");
        JVar in = createFromParcel.param(Parcel.class, "in");
        JVar instance = createFromParcel.body().decl(jclass, "instance", JExpr._new(jclass));
        for (JFieldVar f : jclass.fields().values()) {
            createFromParcel.body().assign(instance.ref(f), JExpr.cast(f.type(), in.invoke("readValue").arg(JExpr.direct(f.type().erasure().name() + ".class.getClassLoader()"))));
        }
        createFromParcel.body()._return(instance);
    }
    
}
