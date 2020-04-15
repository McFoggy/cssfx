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
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.fxmisc.cssfx.impl.CSSFXMonitor;
import org.fxmisc.cssfx.impl.log.CSSFXLogger;
import org.fxmisc.cssfx.impl.monitoring.CleanupDetector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestCSSFXMonitor {

    @BeforeAll
    public static void initJavaFX() throws Exception {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(() -> {
                latch.countDown();
            });
            latch.await(1, TimeUnit.SECONDS);
        } catch (IllegalStateException e) { //Toolkit is already running
        }
    }

    CountDownLatch latch = new CountDownLatch(1);
    Runnable r = () -> latch.countDown();

    @Test
    public void testMonitorStyleSheetsSheetsGetCollected() throws Exception {
        CSSFXLogger.console();
        JMemoryBuddy.memoryTest((checker) -> {
            ObservableList<String> list = FXCollections.observableArrayList();
            String uri = getClass().getResource("bottom.css").toExternalForm();
            list.add(uri);
            new CSSFXMonitor().monitorStylesheets(list);
            CleanupDetector.onCleanup(list, r);
            checker.assertCollectable(list);
        });
        latch.await(1, TimeUnit.SECONDS);
    }
}
