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
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.fxmisc.cssfx.CSSFX;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.CountDownLatch;

@ExtendWith(ApplicationExtension.class)
public class TestMemoryLeaks {


    @Start
    public void init(Stage stage) throws Exception {
        new CSSFXTesterApp().start(stage);
    }

    @Test
    public void testStage() throws Exception {
        JMemoryBuddy.memoryTest(f -> {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                CSSFX.start();
                Stage stage = new Stage();
                StackPane root = new StackPane();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

                String buttonBarCSSUri = getClass().getResource("bottom.css").toExternalForm();
                scene.getStylesheets().add(buttonBarCSSUri);
                root.getStylesheets().add(buttonBarCSSUri);

                f.assertCollectable(stage);
                f.assertCollectable(scene);

                stage.close();
                latch.countDown();
            });
            try {
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            Platform.runLater(() -> cleanupFocusedStage());
        });
    }

    public static void cleanupFocusedStage() {
        // This is a workaround for https://bugs.openjdk.java.net/browse/JDK-8241840
        Stage stage = new Stage();
        stage.setScene(new Scene(new StackPane()));
        stage.show();
        stage.close();
        stage.requestFocus();
    }
}
