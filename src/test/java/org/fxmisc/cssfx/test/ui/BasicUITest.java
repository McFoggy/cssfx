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
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.fxmisc.cssfx.CSSFX;
import org.fxmisc.cssfx.test.misc.DisabledOnMac;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItem;
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
    public void canRetrieveExpectedNodes(FxRobot robot) throws Exception {
        Runnable stopper = basicUI.startCSSFX();
        try {
            assertThat(robot.lookup(".label").queryAll().size(), is(2));
        } finally {
            stopper.run();
        }
    }
    
    @Test
    public void checkCSSIsApplied(FxRobot robot) throws Exception {
        Runnable stopper = basicUI.startCSSFX();

        try {
            Thread.sleep(1000);
            Label cssfxLabel = robot.lookup("#cssfx").queryAs(Label.class);

            Paint textColor = cssfxLabel.getTextFill();
            assertThat("retrieved color is not one of expected", getExpectedTextColor(), hasItem(textColor));
            assertThat(textColor, is(Color.BLUE));
        } finally {
            stopper.run();
        }
    }

    @Test
    @DisabledOnMac
    public void checkCSSFXCanChangeTheLabelFontColor(FxRobot robot) throws Exception {
        // The CSS used by the UI
        URI basicCSS = BasicUI.class.getResource("basic.css").toURI();
        String basicCSSUrl = basicCSS.toURL().toExternalForm();

        // Resources containing the color changes we want to apply to the CSS 
        URI changedBasicCSSBlue = BasicUI.class.getResource("basic-cssfx-blue.css").toURI();
        URI changedBasicCSSRed = BasicUI.class.getResource("basic-cssfx-red.css").toURI();

        // The file we will tell CSSFX to map the CSS to
        Path mappedSourceFile = Files.createTempFile("tmp", ".css");
        mappedSourceFile.toFile().deleteOnExit();
        
        // Let's start with the normal content first 
        Files.copy(Paths.get(changedBasicCSSBlue), mappedSourceFile, StandardCopyOption.REPLACE_EXISTING);
        
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
            // We need to let CSSFX some time to detect the file change
            Thread.sleep(1000);
            
            // Let's check that initial launch has used the mapped
            Label cssfxLabel = robot.lookup("#cssfx").queryAs(Label.class);
            Paint textColor = cssfxLabel.getTextFill();
            assertThat("retrieved color is not one of expected", getExpectedTextColor(), hasItem(textColor));
            assertThat(textColor, is(Color.BLUE));

            // Copy the modified version in to the "source" file
            try {
                Files.copy(Paths.get(changedBasicCSSRed), mappedSourceFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // We TestMemoryLeaksneed to let CSSFX some time to detect the file change
            Thread.sleep(1000);

            textColor = cssfxLabel.getTextFill();
            assertThat("retrieved color is not one of expected", getExpectedTextColor(), hasItem(textColor));
            assertThat(textColor, is(Color.RED));
        } finally {
            stopper.run();
        }
    }
    
    private static Collection<Paint> getExpectedTextColor() {
        return Arrays.asList(Color.WHITE, Color.RED, Color.BLUE);
    }
}
