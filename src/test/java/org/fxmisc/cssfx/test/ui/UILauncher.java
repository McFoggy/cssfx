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


import java.lang.reflect.Method;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class UILauncher extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent p = buildPane();
        Scene s = new Scene(p, 800, 600);
        stage.setScene(s);
        stage.show();
    }
    
    private Parent buildPane() {
        BorderPane bp = new BorderPane();
        
        HBox toolbar = new HBox(10.0);
        TextField tfTestClass = new TextField();
        Button btnLoad = new Button("Load");
        toolbar.getChildren().addAll(tfTestClass, btnLoad);
        HBox.setHgrow(tfTestClass, Priority.ALWAYS);
        
        // org.fxmisc.cssfx.test.ui.BasicUI
        
        ObjectBinding<Class<TestableUI>> tfClass = Bindings.createObjectBinding(() -> {
            String tfClassName = tfTestClass.getText();
            try {
                @SuppressWarnings("unchecked")
                Class<TestableUI> cl = (Class<TestableUI>) Class.forName(tfClassName);
                return cl;
            } catch(ClassNotFoundException ignore) {
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
            return null;
        }, tfTestClass.textProperty());
        
        btnLoad.disableProperty().bind(tfClass.isNull());
        
        btnLoad.setOnAction(ae -> {
            try {
                TestableUI guiTest = tfClass.get().newInstance();
                Method m = guiTest.getClass().getMethod("getRootNode");
                m.setAccessible(true);
                Parent guiTestNode = (Parent) m.invoke(guiTest);
                bp.setCenter(guiTestNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        bp.setTop(toolbar);
        return bp;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
