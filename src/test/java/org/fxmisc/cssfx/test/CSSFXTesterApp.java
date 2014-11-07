package org.fxmisc.cssfx.test;

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

import java.util.Random;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import org.fxmisc.cssfx.CSSFX;
import org.fxmisc.cssfx.test.ui.TestableUI;

public class CSSFXTesterApp extends Application implements TestableUI {
    private Button btnLoadOddCSS;   // needed as field for tests purposes

    @Override
    public void start(Stage stage) throws Exception {
        fillStage(stage);
        stage.show();
        CSSFX.start();
    }

    public void initUI(Stage stage) {
        Scene s = stage.getScene(); 

        String cssURI = getClass().getResource("app.css").toExternalForm();
        s.getStylesheets().add(cssURI);
        
        btnLoadOddCSS.setOnAction((ae) -> s.getStylesheets().add(getClass().getResource("oddeven.css").toExternalForm()));
    }

    private void fillStage(Stage stage) {
        Parent p = getRootNode();
        Scene scene = new Scene(p, 500, 350);
        stage.setScene(scene);

        initUI(stage);
    }

    private Node createButtonBar() {
        FlowPane fp = new FlowPane();
        fp.getStyleClass().addAll("button-bar", "bottom");
        fp.getChildren().addAll(new Button("Action"), new Button("Action"));
        String buttonBarCSSUri = getClass().getResource("bottom.css").toExternalForm();
        fp.getStylesheets().add(buttonBarCSSUri);

        return fp;
    }

    private Group buildCirclePane(int prefWidth, int prefHeight) {
        Group freePlacePane = new Group();
        int defaultShapeSize = 50;
        int shapeNumber = 10;
        Random r = new Random();

        for (int i = 0; i < shapeNumber; i++) {
            Circle c = new Circle(Math.max(10, defaultShapeSize * r.nextInt(100) / 100));
            c.getStyleClass().add("circle");
            if (i % 2 == 0) {
                c.getStyleClass().add("even");
            } else {
                c.getStyleClass().add("odd");
            }
            c.setCenterX(r.nextInt(prefWidth));
            c.setCenterY(r.nextInt(prefHeight));
            c.setFill(Color.BLUE);
            freePlacePane.getChildren().add(c);
        }

        freePlacePane.getStyleClass().add("circles");
        freePlacePane.prefWidth(250);
        freePlacePane.prefWidth(200);
        return freePlacePane;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public Parent getRootNode() {
        BorderPane bp = new BorderPane();

        int prefWidth = 300;
        int prefHeight = 200;

        Button btnShowBottomBar = new Button("Dynamic bottom bar");
        btnShowBottomBar.setId("dynamicBar");
        btnShowBottomBar.setOnAction((ae) -> bp.setBottom(createButtonBar()));
        btnLoadOddCSS = new Button("Load additional CSS");
        btnLoadOddCSS.setId("dynamicCSS");
        Button btnCreateStage = new Button("Create new stage");
        btnCreateStage.setOnAction(ae -> {
            Stage stage = new Stage();
            fillStage(stage);
            stage.show();
        });
        btnCreateStage.setId("dynamicStage");
        FlowPane topBar = new FlowPane(btnShowBottomBar, btnLoadOddCSS, btnCreateStage);

        topBar.getStyleClass().addAll("button-bar", "top");

        bp.setTop(topBar);
        bp.setCenter(buildCirclePane(prefWidth, prefHeight));
        return bp;
    }
}