package org.fxmisc.cssfx.impl;

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


import static org.fxmisc.cssfx.impl.log.CSSFXLogger.logger;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.stage.Window;
import org.fxmisc.cssfx.api.URIToPathConverter;
import org.fxmisc.cssfx.impl.events.CSSFXEvent;
import org.fxmisc.cssfx.impl.events.CSSFXEvent.EventType;
import org.fxmisc.cssfx.impl.events.CSSFXEventListener;
import org.fxmisc.cssfx.impl.monitoring.CleanupDetector;
import org.fxmisc.cssfx.impl.monitoring.PathsWatcher;

/**
 * CSSFXMonitor is the central controller of the CSS monitoring feature.   
 *   
 * @author Matthieu Brouillard
 */
public class CSSFXMonitor {
    private PathsWatcher pw;

    // keep insertion order
    private List<URIToPathConverter> knownConverters = new CopyOnWriteArrayList<>();
    private ObservableList<? extends Window> windows;
    private ObservableList<Scene> scenes;
    private ObservableList<Node> nodes;
    private List<CSSFXEventListener> eventListeners = new CopyOnWriteArrayList<>();

    public CSSFXMonitor() {
    }
    
    public void setStages(ObservableList<Stage> stages) {
        setWindows(stages);
    }

    public void setWindows(ObservableList<? extends Window> stages) {
        this.windows = stages;
    }

    public void setScenes(ObservableList<Scene> scenes) {
        this.scenes = scenes;
    }

    public void setNodes(ObservableList<Node> nodes) {
        this.nodes = nodes;
    }

    public void addAllConverters(Collection<URIToPathConverter> converters) {
        knownConverters.addAll(converters);
    }

    public void addAllConverters(URIToPathConverter... converters) {
        knownConverters.addAll(Arrays.asList(converters));
    }

    public void addConverter(URIToPathConverter newConverter) {
        knownConverters.add(newConverter);
    }

    public void removeConverter(URIToPathConverter converter) {
        knownConverters.remove(converter);
    }

    public void addEventListener(CSSFXEventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(CSSFXEventListener listener) {
        eventListeners.remove(listener);
    }

    public void start() {
        logger(CSSFXMonitor.class).info("CSS Monitoring is about to start");

        pw = new PathsWatcher();

        // start to monitor stage changes
        if (windows != null) {
            monitorWindows(windows);
        } else if (scenes != null) {
            monitorScenes(scenes);
        } else if (nodes != null) {
            monitorChildren(nodes);
        }

        pw.watch();
        logger(CSSFXMonitor.class).info("CSS Monitoring started");
    }
    
    public void stop() {
        pw.stop();
    }

    private void monitorWindows(ObservableList<? extends Window> observableWindows) {
        // first listen for changes
        observableWindows.addListener(new ListChangeListener<Window>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends Window> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (Window addedWindow : c.getAddedSubList()) {
                            monitorWindow(addedWindow);
                        }
                    }
                }
            }
        });

        // then process already existing stages
        for (Window stage : observableWindows) {
            monitorWindow(stage);
        }
    }

    WeakHashMap<Window,Integer> weakWindowSet = new WeakHashMap<>();
    private void monitorWindow(Window stage) {
        if(!weakWindowSet.containsKey(stage)) {
            weakWindowSet.put(stage,1);
            eventNotify(CSSFXEvent.newEvent(EventType.STAGE_ADDED, stage));
            monitorSceneProperty(stage.sceneProperty());
        }
    }

    private void monitorSceneProperty(ReadOnlyObjectProperty<Scene> stageSceneProperty) {
        // first listen to changes
        stageSceneProperty.addListener(new ChangeListener<Scene>() {
            @Override
            public void changed(ObservableValue<? extends Scene> ov, Scene o, Scene n) {
                if (n != null) {
                    monitorScene(n);
                }
            }
        });

        if (stageSceneProperty.getValue() != null) {
            monitorScene(stageSceneProperty.getValue());
        }
    }

    private void monitorScenes(ObservableList<Scene> observableScenes) {
        // first listen for changes
        observableScenes.addListener(new ListChangeListener<Scene>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends Scene> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (Scene addedScene : c.getAddedSubList()) {
                            monitorScene(addedScene);
                        }
                    }
                }
            }
        });

        // then add existing values
        for (Scene s : observableScenes) {
            monitorScene(s);
        }
    }

    WeakHashMap<Scene,Integer> weakSceneSet = new WeakHashMap<>();
    private void monitorScene(Scene scene) {
        if(!weakSceneSet.containsKey(scene)) {
            weakSceneSet.put(scene, 1);
            eventNotify(CSSFXEvent.newEvent(EventType.SCENE_ADDED, scene));

            monitorStylesheets(scene.getStylesheets());
            monitorRoot(scene.rootProperty());
        }
    }


    private void monitorRoot(ObjectProperty<Parent> rootProperty) {
        // register on modification
        rootProperty.addListener((ov, o, n) -> {
            if (n != null) {
                monitorNode(n);
            }
        });

        // check current value
        if (rootProperty.getValue() != null) {
            monitorNode(rootProperty.getValue());
        }
    }

    WeakHashMap<Node,Integer> weakNodeSet = new WeakHashMap<>();
    private void monitorNode(Node node) {
        if(!weakNodeSet.containsKey(node)) {
            weakNodeSet.put(node, 1);
            if (node instanceof Parent) {
                Parent p = (Parent) node;
                monitorStylesheets(p.getStylesheets());
                monitorChildren(p.getChildrenUnmodifiable());
            }
            eventNotify(CSSFXEvent.newEvent(EventType.NODE_ADDED, node));
        }
    }
    


    private void monitorChildren(ObservableList<Node> childrenUnmodifiable) {
        // first listen to changes
        childrenUnmodifiable.addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends Node> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (Node addedNode : c.getAddedSubList()) {
                            monitorNode(addedNode);
                        }
                    }
                }
            }
        });
        // then look already existing children
        for (Node node : childrenUnmodifiable) {
            monitorNode(node);
        }
    }

    public void monitorStylesheets(ObservableList<String> stylesheets) {
        final URIRegistrar registrar = new URIRegistrar(knownConverters, pw);

        // first register for changes
        stylesheets.addListener(new StyleSheetChangeListener(registrar));

        // then look already set stylesheets uris
        for (String uri : stylesheets) {
            registrar.register(uri, stylesheets);
        }

        CleanupDetector.onCleanup(stylesheets, () -> {
            Platform.runLater(() -> {
                // This is important, so no empty "Runnables" build up in the PathsWatcher
                registrar.cleanup();
            });
        });
    }



    private void eventNotify(CSSFXEvent<?> e) {
        for (CSSFXEventListener listener : eventListeners) {
            listener.onEvent(e);
        }
    }

    public static class URIRegistrar {
        final Map<String, Path> sourceURIs = new HashMap<>();
        final Map<Path, List<Runnable>> actions = new HashMap<>();
        final List<URIToPathConverter> converters;
        private PathsWatcher wp;

        public URIRegistrar(List<URIToPathConverter> c, PathsWatcher wp) {
            converters = c;
            this.wp = wp;
        }

        public void register(String uri, ObservableList<? extends String> stylesheets) {
            if (!sourceURIs.containsKey(uri)) {
                logger(CSSFXMonitor.class).debug("searching source for css[%s]", uri);
                for (URIToPathConverter c : converters) {
                    Path sourceFile = c.convert(uri);
                    List<Runnable> runnables = new LinkedList<>();
                    if (sourceFile != null) {
                        logger(CSSFXMonitor.class).info("css[%s] will be mapped to source[%s]", uri, sourceFile);
                        Path directory = sourceFile.getParent();

                        Runnable r = new URIStyleUpdater(uri, sourceFile.toUri().toString(), (ObservableList<String>) stylesheets);
                        wp.monitor(directory.toAbsolutePath().normalize(), sourceFile.toAbsolutePath().normalize(), r);
                        runnables.add(r);
                        Platform.runLater(() -> {
                            r.run();
                        });

                        sourceURIs.put(sourceFile.toUri().toString(), sourceFile);
                    }
                    actions.put(sourceFile,runnables);
                }
            }
        }

        public void unregister(String uri) {
        }


        public void cleanup() {
            actions.forEach((path,runnables) -> {
                runnables.forEach( runnable -> {
                    wp.unregister(path.getParent().toAbsolutePath().normalize(), path.toAbsolutePath().normalize(), runnable);
                });
            });
        }

    }

    private static class StyleSheetChangeListener implements ListChangeListener<String> {
        private URIRegistrar registrar;

        private StyleSheetChangeListener(URIRegistrar registrar) {
            this.registrar = registrar;
        }

        @Override
        public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
            while (c.next()) {
                if (c.wasRemoved()) {
                    for (String removedURI : c.getRemoved()) {
                        registrar.unregister(removedURI);
                    }
                }
                if (c.wasAdded()) {
                    for (String newURI : c.getAddedSubList()) {
                        registrar.register(newURI, c.getList());
                    }
                }
            }
        }
    }


    public static class URIStyleUpdater implements Runnable {
        private final String sourceURI;
        private final String originalURI;
        private final WeakReference<ObservableList<String>> cssURIsWeak;

        public URIStyleUpdater(String originalURI, String sourceURI, ObservableList<String> cssURIs) {
            this.originalURI = originalURI;
            this.sourceURI = sourceURI;
            this.cssURIsWeak = new WeakReference<>(cssURIs);
        }

        @Override
        public void run() {
            IntegerProperty positionIndex = new SimpleIntegerProperty();
            ObservableList<String> cssURIs = cssURIsWeak.get();

            if(cssURIs != null) {
                Platform.runLater(() -> {
                    positionIndex.set(cssURIs.indexOf(originalURI));
                    if (positionIndex.get() != -1) {
                        cssURIs.remove(originalURI);
                    }
                    if (positionIndex.get() == -1) {
                        positionIndex.set(cssURIs.indexOf(sourceURI));
                    }
                    cssURIs.remove(sourceURI);
                });
                Platform.runLater(() -> {
                    if (positionIndex.get() >= 0) {
                        cssURIs.add(positionIndex.get(), sourceURI);
                    } else {
                        cssURIs.add(sourceURI);
                    }
                });
            }
        }
    }
}
