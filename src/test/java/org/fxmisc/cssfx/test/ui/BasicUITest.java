package org.fxmisc.cssfx.test.ui;

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


import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.fxmisc.cssfx.CSSFX;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(ApplicationExtension.class)
public class BasicUITest {
    private BasicUI basicUI;

    @Start
    public void init(Stage stage) throws Exception {
        basicUI = new BasicUI();
        basicUI.start(stage);
    }

    @Test
    public void canRetrieveExpectedNodes(FxRobot robot) {
        Runnable stopper = basicUI.startCSSFX();
        try {
            assertThat(robot.lookup(".label").queryAll().size(), is(2));
        } finally {
            stopper.run();
        }
    }
    
    @Test
    public void checkCSSIsApplied(FxRobot robot) {
        Runnable stopper = basicUI.startCSSFX();
        try {
            Label cssfxLabel = robot.lookup("#cssfx").queryAs(Label.class);
            assertThat(cssfxLabel.getTextFill(), is(Color.WHITE));
        } finally {
            stopper.run();
        }
    }

    /*
    @Test
    public void checkCSSFXCanChangeTheLabelFontColor(FxRobot robot) throws Exception {
        // The CSS used by the UI
        URI basicCSS = BasicUI.class.getResource("basic.css").toURI();
        String basicCSSUrl = basicCSS.toURL().toExternalForm();
        
        // The file we will tell CSSFX to map the CSS to
        Path mappedSourceFile = Files.createTempFile("tmp", ".css");
        mappedSourceFile.toFile().deleteOnExit();
        
        // Let's start with the normal content first 
        Files.copy(Paths.get(basicCSS), mappedSourceFile, StandardCopyOption.REPLACE_EXISTING);

        // a resource containing the required changes we want to apply to the CSS 
        URI changedBasicCSS = BasicUI.class.getResource("basic-cssfx.css").toURI();
        
        // start CSSFX
        Runnable stopper = CSSFX.onlyFor(basicUI.getRootNode())
                .noDefaultConverters()
                .addConverter((uri) -> {
                    if (basicCSSUrl.equals(uri)) {
                        return mappedSourceFile;
                    }
                    return null;
                }).start();
        try {
            // Copy the modified version in to the "source" file
            Files.copy(Paths.get(changedBasicCSS), mappedSourceFile, StandardCopyOption.REPLACE_EXISTING);

            // We need to let CSSFX some time to detect the file change
            // TODO check if waiting is really needed
            Thread.sleep(2000);

            Label cssfxLabel = robot.lookup("#cssfx").queryAs(Label.class);
            assertThat(cssfxLabel.getTextFill(), is(Color.BLUE));
        } finally {
            stopper.run();
        }
    }
    */
}
