package org.fxmisc.cssfx.impl;

/*
 * #%L
 * CSSFX
 * %%
 * Copyright (C) 2014 CSSFX by Matthieu Brouillard
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


import static java.util.stream.Collectors.toList;
import static org.fxmisc.cssfx.impl.log.CSSFXLogger.logger;

import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ApplicationStages {
    /**
     * @deprecated do not use this method anymore,
     * @see Window#getWindows for similar functionnality
     */
    @Deprecated(forRemoval = true, since = "11.0.2")
    public static ObservableList<Stage> monitoredStages(Stage ... restrictedTo) {
        try {
            ObservableList<Stage> stages = Window.getWindows().stream()
                    .map(Stage.class::cast)
                    .collect(
                            Collectors.collectingAndThen(toList(), FXCollections::observableArrayList)
                    );
            logger(ApplicationStages.class).debug("successfully retrieved JavaFX stages by calling javafx.stage.Window.getWindows()");
            return stages;
        } catch (Exception e) {
            logger(ApplicationStages.class).error("cannot observe stages changes by calling javafx.stage.Window.getWindows()", e);
        }
        return FXCollections.emptyObservableList();
    }
}
