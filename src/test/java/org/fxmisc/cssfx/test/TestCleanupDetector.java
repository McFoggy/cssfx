package org.fxmisc.cssfx.test;

/*
 * #%L
 * CSSFX
 * %%
 * Copyright (C) 2014 - 2020 CSSFX by Matthieu Brouillard
 * %%
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
 * #L%
 */

import de.sandec.jmemorybuddy.JMemoryBuddy;
import javafx.scene.paint.Color;
import org.fxmisc.cssfx.impl.monitoring.CleanupDetector;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestCleanupDetector {

    int counter = 0;

    @Test
    public void isRunnableCalled() throws Exception{

        Runnable r = () -> {
            counter += 1;
        };

        JMemoryBuddy.memoryTest(checker -> {
            Object o = new Object();
            CleanupDetector.onCleanup(o, r);
            checker.assertCollectable(o);
        });


        assertThat(counter, is(1));
    }
}
