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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import org.fxmisc.cssfx.CSSFX;
import org.fxmisc.cssfx.CSSFX.Stoppable;

public class CSSFXTesterApp extends Application {

	@Override
	public void start(Stage stage) throws Exception {
	    BorderPane bp = new BorderPane();
		
		int prefWidth = 300;
		int prefHeight = 200;
		
		Button btnShowBottomBar = new Button("load bottom bar");
		btnShowBottomBar.setOnAction((ae) -> bp.setBottom(createButtonBar()));
		Button btnLoadOddCSS = new Button("load odd.css");
		FlowPane topBar = new FlowPane(btnShowBottomBar, btnLoadOddCSS);
		
		topBar.getStyleClass().addAll("button-bar", "top");
		
		bp.setTop(topBar);
		bp.setCenter(buildCirclePane(prefWidth, prefHeight));
		bp.setRight(buildPropertyPane());
		
		Scene s = new Scene(bp, 500, 350);

        btnLoadOddCSS.setOnAction((ae) -> s.getStylesheets().add(getClass().getResource("oddeven.css").toExternalForm()));
		
		String cssURI = getClass().getResource("app.css").toExternalForm();
		s.getStylesheets().add(cssURI);
		stage.setScene(s);
		stage.show();
		
		Stoppable monitor = CSSFX.monitor(s).start();
		stage.setOnCloseRequest((we) -> monitor.stop());
	}

    private Node createButtonBar() {
        FlowPane fp = new FlowPane();
        fp.getStyleClass().addAll("button-bar","bottom");
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
			if (i%2 == 0) {
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

	private Node buildPropertyPane() {
	    VBox vertical = new VBox();
        vertical.setMinWidth(100.0);
	    
	    vertical.getStyleClass().add("properties");
	    vertical.getChildren().addAll(new Label("Properties"),new TextField("property"), new TextField("property"), new TextField("property"));
	    
	    String paneCSSUri = getClass().getResource("pane.css").toExternalForm();
	    vertical.getStylesheets().add(paneCSSUri);
	    
        return vertical;
    }

    public static void main(String[] args) {
		launch(args);
	}
}
