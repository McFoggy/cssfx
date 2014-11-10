package org.fxmisc.cssfx.impl.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.fxmisc.cssfx.impl.ui.model.JavaFXVM;
import org.fxmisc.cssfx.impl.ui.model.JavaFXVMModel;
import org.fxmisc.cssfx.impl.ui.threads.ExternalJavaFXThreadsDetector;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

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


public class CSSFXApplication extends Application {
    public static void main(String[] args) throws Exception {
        JavaFXVMModel m = new JavaFXVMModel();
        ExternalJavaFXThreadsDetector detector = new ExternalJavaFXThreadsDetector(m);
        
        m.fxVMs().addListener(new ListChangeListener<JavaFXVM>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends JavaFXVM> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (JavaFXVM javaFXVM : c.getAddedSubList()) {
                            System.out.println("new JavaFX VM detected: " + javaFXVM.getId() + " - " + javaFXVM.getName());
                        }
                    }
                    if (c.wasRemoved()) {
                        for (JavaFXVM javaFXVM : c.getRemoved()) {
                            System.out.println("JavaFX VM ended: " + javaFXVM.getId());
                        }
                    }
                }
            }
        });
        
        Thread monitorVMs = new Thread(detector);
        monitorVMs.setDaemon(true);
        monitorVMs.start();

        System.in.read();
    }

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane bp = new BorderPane();
        bp.setCenter(new Label("Simple"));
        
        Scene scene = new Scene(bp, 300, 200);
        stage.setScene(scene);
        stage.show();
    }
}
