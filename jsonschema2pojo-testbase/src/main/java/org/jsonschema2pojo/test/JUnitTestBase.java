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

package org.jsonschema2pojo.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class JUnitTestBase {
    private AutoCloseable mocksClosable = null;
    private final Lock lock = new ReentrantLock(); // to avoid race conditions, not strictly necessary

    /**
     * it's better to close mocks after tests to make all mock engines happy
     */
    @BeforeEach
    public final void openMocks() {
        lock.lock();
        if (mocksClosable == null) {
            mocksClosable = MockitoAnnotations.openMocks(this);
        }
        lock.unlock();
    }

    /**
     * it's better to close mocks after tests to make all mock engines happy
     */
    @AfterEach
    public final void closeMocks() throws Exception {
        lock.lock();
        if (mocksClosable != null) {
            try {
                mocksClosable.close();
            } finally {
                mocksClosable = null;
            }
        }
        lock.unlock();
    }

}
