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
import org.fxmisc.cssfx.impl.log.CSSFXLogger;
import org.fxmisc.cssfx.impl.monitoring.PathsWatcher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestURIRegistrar {

    private final List<URIToPathConverter> converters = Arrays.asList(URIToPathConverters.DEFAULT_CONVERTERS);
    private PathsWatcher pw = new PathsWatcher();

    @Test
    public void testURIRegistrar() {
        JMemoryBuddy.memoryTest(checker -> {
            ObservableList<String> list = FXCollections.observableArrayList();
            String uri = getClass().getResource("bottom.css").toExternalForm();
            list.add(uri);

            CSSFXMonitor.URIRegistrar registrar = new CSSFXMonitor.URIRegistrar(converters, pw);
            registrar.register(uri,list);

            checker.assertCollectable(list);
        });

        JMemoryBuddy.memoryTest(checker -> {
            ObservableList<String> list = FXCollections.observableArrayList();
            String uri = getClass().getResource("bottom.css").toExternalForm();
            list.add(uri);

            CSSFXMonitor.URIRegistrar registrar = new CSSFXMonitor.URIRegistrar(converters, pw);
            registrar.register(uri,list);
            registrar.cleanup();

            checker.assertCollectable(list);
        });
    }
}
