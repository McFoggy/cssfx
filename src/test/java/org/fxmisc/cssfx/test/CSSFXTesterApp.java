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

import org.fxmisc.cssfx.CSSFX;
import org.fxmisc.cssfx.CSSFX.Stoppable;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class CSSFXTesterApp extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		Group freePlacePane = new Group();
		
		int prefWidth = 400;
		int prefHeight = 300;
		
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
		
		Scene s = new Scene(freePlacePane, prefWidth, prefHeight);
		String cssURI = getClass().getResource("app.css").toExternalForm();
		s.getStylesheets().add(cssURI);
		stage.setScene(s);
		stage.show();
		
		Stoppable monitor = CSSFX.monitor(s).start();
		stage.setOnCloseRequest((we) -> monitor.stop());
	}

	public static void main(String[] args) {
		launch(args);
	}
}
