/**
 * Copyright Â© 2010-2014 Nokia
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class PackageUtilTest {

    @Test
    public void refToHttpResourceIsNotParsed() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "http://jsonschema2pojo.googlecode.com/git-history/jsonschema2pojo-0.3.4/jsonschema2pojo-integration-tests/src/test/resources/schema/ref/refsToA.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo(packageBase)));
    }

    @Test
    public void refToHttpsResourceIsNotParsed() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "https://jsonschema2pojo.googlecode.com/git-history/jsonschema2pojo-0.3.4/jsonschema2pojo-integration-tests/src/test/resources/schema/ref/refsToA.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo(packageBase)));
    }

    @Test
    public void refToJavaResourceIsNotParsed() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "java://jsonschema2pojo.googlecode.com/git-history/jsonschema2pojo-0.3.4/jsonschema2pojo-integration-tests/src/test/resources/schema/ref/refsToA.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo(packageBase)));
    }

    @Test
    public void refToClasspathResourceIsNotParsed() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "classpath://jsonschema2pojo.googlecode.com/git-history/jsonschema2pojo-0.3.4/jsonschema2pojo-integration-tests/src/test/resources/schema/ref/refsToA.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo(packageBase)));
    }

    @Test
    public void refToResourceTypeResourceIsNotParsed() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "resource://jsonschema2pojo.googlecode.com/git-history/jsonschema2pojo-0.3.4/jsonschema2pojo-integration-tests/src/test/resources/schema/ref/refsToA.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo(packageBase)));
    }

    @Test
    public void refToFileResourceIsNotParsed() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "file://jsonschema2pojo.googlecode.com/git-history/jsonschema2pojo-0.3.4/jsonschema2pojo-integration-tests/src/test/resources/schema/ref/refsToA.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo(packageBase)));
    }

    @Test
    public void resolveDoesntFailOnNullArguments() throws NoSuchMethodException {

        // when
        String result = PackageUtil.resolve(null, null);

        // then
        assertThat(result, not(equalTo(null)));
    }

    @Test
    public void resolveDoesntFailOnNullBasePackage() throws NoSuchMethodException {

        // given
        String refAsText = "file://jsonschema2pojo.googlecode.com/git-history/jsonschema2pojo-0.3.4/jsonschema2pojo-integration-tests/src/test/resources/schema/ref/refsToA.json";

        // when
        String result = PackageUtil.resolve(null, refAsText);

        // then
        assertThat(result, not(equalTo(null)));
    }

    @Test
    public void resolveDoesntFailOnNullRef() throws NoSuchMethodException {

        // when
        String packageBase = "com.example";
        String result = PackageUtil.resolve(packageBase, null);

        // then
        assertThat(result, not(equalTo(null)));
    }

    @Test
    public void resolveReturnsBasePackageOnArgumentNotContainingDots() throws NoSuchMethodException {

        // given
        String packageBase = "com";

        // when
        String result = PackageUtil.resolve(packageBase, null);

        // then
        assertThat(result, is(equalTo(packageBase)));
    }

    @Test
    public void resolveReturnsBasePackageOnArgumentContainingDots() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";

        // when
        String result = PackageUtil.resolve(packageBase, null);

        // then
        assertThat(result, is(equalTo(packageBase)));
    }

    @Test
    public void resolveReturnsDefaultPackageOnParentDirRef() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "../any.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo("com")));
    }

    @Test
    public void resolveReturnsDefaultPackageOnParentChildDirRef() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "../foo/any.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo("com.foo")));
    }

    @Test
    public void resolveReturnsDefaultPackageOnParentRefReachingTooHigh() throws NoSuchMethodException {

        // given
        String packageBase = "com";
        String refAsText = "../../foo/any.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo("")));
    }

    @Test
    public void resolveReturnsProperPackageOnDefaultPackageChilsRef() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "../../foo/any.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo("foo")));
    }

    @Test
    public void resolveReturnsBasePackageOnLocalRef() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "#/foo/any";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo(packageBase)));
    }

    @Test
    public void resolveReturnsProperPackageOnRefEndingWithHash() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "foo/any.json#";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo(packageBase + ".foo")));
    }

    @Test
    public void resolveReturnsProperPackageOnRefContainingHash() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "foo/any.json#/bar/any";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo(packageBase + ".foo")));
    }

    @Test
    public void resolveReturnsBasePackageOnRefToCurrentDir() throws NoSuchMethodException {

        // given
        String packageBase = "com.example";
        String refAsText = "any.json";

        // when
        String result = PackageUtil.resolve(packageBase, refAsText);

        // then
        assertThat(result, is(equalTo(packageBase)));
    }
}
