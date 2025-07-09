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

package org.jsonschema2pojo.integration.util;

import java.lang.reflect.Field;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public final class ParcelUtils {

    private ParcelUtils() {
    }
    
    public static Parcel writeToParcel(Parcelable instance, String key) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(key, instance);
        
        Parcel parcel = Parcel.obtain();
        parcel.writeBundle(bundle);
        return parcel;
    }

    public static Parcel parcelableWriteToParcel(Parcelable instance) {
        Parcel parcel = Parcel.obtain();
        instance.writeToParcel(parcel, 0);
        return parcel;
    }
    
    public static Parcelable readFromParcel(Parcel parcel, Class<?> parcelableType, String key) {
        parcel.setDataPosition(0);

        Bundle bundle = parcel.readBundle();
        bundle.setClassLoader(parcelableType.getClassLoader());

        return bundle.getParcelable(key);
    }

    public static Parcelable parcelableReadFromParcel(Parcel parcel, Parcelable parcelable) {
        parcel.setDataPosition(0);
        return createFromParcelFromParcelable(parcel, parcelable);
    }


    private static Parcelable createFromParcelFromParcelable(Parcel in, Parcelable parcelable) {
        try {
            Class<?> parcelableClass = parcelable.getClass().getClassLoader().loadClass(parcelable.getClass().getName());
            Field creatorField = parcelableClass.getDeclaredField("CREATOR");
            Parcelable.Creator<?> creatorInstance = (Parcelable.Creator<?>) creatorField.get(parcelable);

            return (Parcelable) creatorInstance.createFromParcel(in);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
