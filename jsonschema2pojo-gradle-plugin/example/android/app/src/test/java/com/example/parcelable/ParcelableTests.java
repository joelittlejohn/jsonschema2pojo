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

package com.example.parcelable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotSame;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.parcelable.Book;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE, sdk=28)
public class ParcelableTests {

    @Test
    public void testParcelable() {
        Book book = new Book();
        book.setAuthor("Author");
        book.setTitle("Title");

        Parcel parcel = parcelableWriteToParcel(book);

        Book unparceledBook = Book.CREATOR.createFromParcel(parcel);
        assertThat(unparceledBook.getAuthor(), equalTo(book.getAuthor()));
        assertThat(unparceledBook.getTitle(), equalTo(book.getTitle()));
        assertThat(unparceledBook, is(equalTo(book)));
        assertNotSame(book, unparceledBook);
    }

    private Parcel parcelableWriteToParcel(Parcelable instance) {
        Parcel parcel = Parcel.obtain();
        instance.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        return parcel;
    }

}
