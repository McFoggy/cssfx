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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.fxmisc.cssfx.api.URIToPathConverter;
import org.fxmisc.cssfx.impl.CSSFXMonitor;
import org.fxmisc.cssfx.impl.URIToPathConverters;
import org.fxmisc.cssfx.impl.monitoring.PathsWatcher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TestURIStyleUpdater {

    @Test
    public void testIsWeak() {
        JMemoryBuddy.memoryTest(checker -> {
            ObservableList<String> list = FXCollections.observableArrayList();
            list.add("a");
            list.add("a");
            CSSFXMonitor.URIStyleUpdater updater = new CSSFXMonitor.URIStyleUpdater("a","aa", list);

            checker.setAsReferenced(updater);
            checker.assertCollectable(list);
        });
    }
}
