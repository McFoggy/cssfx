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
import org.fxmisc.cssfx.impl.log.CSSFXLogger;
import org.fxmisc.cssfx.impl.monitoring.PathsWatcher;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

public class TestPathsWatcher {

    @Test
    void testPathsWatcher() {
        PathsWatcher watcher = new PathsWatcher();
        Path directory = new File(".").toPath();
        Path file = new File("./pom.xml").toPath();

        JMemoryBuddy.memoryTest(checker -> {
            Runnable r = new EmptyRunnable();
            watcher.monitor(directory,file,r);
            checker.assertNotCollectable(r);
            r = null;
        });

        JMemoryBuddy.memoryTest(checker -> {
            Runnable r = new EmptyRunnable();
            watcher.monitor(directory,file,r);
            watcher.unregister(directory, file,r);
            checker.assertCollectable(r);
            r = null;
        });

    }

    public static class EmptyRunnable implements Runnable{
        @Override
        public void run() {

        }
    }
}
