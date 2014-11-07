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



import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import org.fxmisc.cssfx.CSSFX;
import org.junit.Test;


public class BasicUITest extends AbstractTestableGUITest {
    public BasicUITest() {
        super(BasicUI.class);
    }
    
    @Test
    public void canRetrieveExpectedNodes() {
        assertThat(findAll(".label").size(), is(2));
    }
    
    @Test
    public void checkCSSIsApplied() {
        Label cssfxLabel = find("#cssfx");
        assertThat(cssfxLabel.getTextFill(), is(Color.WHITE));
    }
    
    @Test
    public void checkCSSFXCanChangeTheLabelFontColor() throws Exception {
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
        Runnable stopper = CSSFX.onlyFor(builtRootNode())
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
            sleep(100);
            
            Label cssfxLabel = find("#cssfx");
            assertThat(cssfxLabel.getTextFill(), is(Color.BLUE));
        } finally {
            stopper.run();
        }
    }
}
