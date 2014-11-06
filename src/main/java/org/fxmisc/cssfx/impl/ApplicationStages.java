package org.fxmisc.cssfx.impl;

import static org.fxmisc.cssfx.impl.log.CSSFXLogger.logger;

import java.lang.reflect.Method;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class ApplicationStages {
    public static ObservableList<Stage> monitoredStages(Stage ...restrictedTo) {
        try {
            Class<?> sh = Class.forName("com.sun.javafx.stage.StageHelper");
            Method m = sh.getMethod("getStages");
            ObservableList<Stage> stages = (ObservableList<Stage>) m.invoke(null, new Object[0]);
            logger(ApplicationStages.class).debug("successfully retrieved JavaFX stages from com.sun.javafx.stage.StageHelper");
            return stages;
        } catch (Exception e) {
            logger(ApplicationStages.class).error("cannot observe stages changes by calling com.sun.javafx.stage.StageHelper.getStages()", e);
        }
        return FXCollections.emptyObservableList();
    }
}
