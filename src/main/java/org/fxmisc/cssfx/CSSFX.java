package org.fxmisc.cssfx;

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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import org.fxmisc.cssfx.paths.PathsWatcher;

public class CSSFX {
    public static Monitor monitor(Scene s) {
        Monitors monitors = new Monitors();

        Monitor m = new Monitor(monitors);
        m.setRoot(s.getRoot());
        m.registerCSSs(s.getStylesheets());
        monitors.monitors().add(m);

        return m;
    }

    public static class Monitor {
        private Monitors monitors;
        private Parent root;
        private List<Function<String, Path>> uriToFileCreators = new LinkedList<Function<String, Path>>();

        private ObservableList<String> stylesheets;

        private Monitor(Monitors chain) {
            this.monitors = chain;
            uriToFileCreators.add(CSSFX::mavenResourceFileFromURI);
            uriToFileCreators.add(CSSFX::gradleResourceFileFromURI);
            uriToFileCreators.add(CSSFX::jarResourceFileFromURI);
        }

        void registerCSSs(ObservableList<String> stylesheets) {
            this.stylesheets = stylesheets;
        }

        void setRoot(Parent root) {
            this.root = root;
        }

        Parent getRoot() {
            return root;
        }

        ObservableList<String> stylesheets() {
            return stylesheets;
        }

        public Monitor addURIResolver(Function<String, Path> resolver) {
            uriToFileCreators.add(0, resolver);
            return this;
        }

        public Stoppable start() {
            return monitors.start();
        }

        public Monitor monitor(Scene s) {
            Monitor m = new Monitor(monitors);
            m.setRoot(s.getRoot());
            m.registerCSSs(s.getStylesheets());
            monitors.monitors().add(m);

            return m;
        }
    }

    public static class Monitors {
        private ObservableList<Monitor> monitors = FXCollections.observableArrayList();
        private Set<String> knownURIs = new HashSet<>();
        private Set<Node> knownNodes = Collections.newSetFromMap(new WeakHashMap<Node, Boolean>());

        private PathsWatcher pw;

        private Monitors() {
        }

        public Stoppable start() {
            pw = new PathsWatcher();

            for (Monitor monitor : monitors) {
                ObservableList<String> stylesheets = monitor.stylesheets();
                List<Function<String, Path>> uriToFileCreators = monitor.uriToFileCreators;

                final ListChangeListener<String> styleSheetChangeListener = new ListChangeListener<String>() {
                    @Override
                    public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
                        while (c.next()) {
                            if (c.wasAdded()) {
                                List<? extends String> newURIs = c.getAddedSubList();
                                for (String newURI : newURIs) {
                                    registerURI(newURI, stylesheets, uriToFileCreators);
                                }
                            }
                        }
                    }
                };

                monitorStylesheets(stylesheets, uriToFileCreators, styleSheetChangeListener);

                Parent monitorRoot = monitor.getRoot();
                monitorParent(monitorRoot, monitor, styleSheetChangeListener);
            }

            pw.watch();
            return pw::stop;
        }

        private void monitorParent(Parent p, Monitor monitor, ListChangeListener<String> styleSheetChangeListener) {
            if (p != null && !knownNodes.contains(p)) {
                knownNodes.add(p);
                monitorChildren(p, monitor, styleSheetChangeListener);
            }
        }

        private void monitorStylesheets(ObservableList<String> stylesheets, List<Function<String, Path>> uriToFileCreators,
                final ListChangeListener<String> styleSheetChangeListener) {
            List<String> fixedStylesheets = new LinkedList<String>(stylesheets);
            for (String uri : fixedStylesheets) {
                registerURI(uri, stylesheets, uriToFileCreators);
            }
            stylesheets.addListener(styleSheetChangeListener);
        }

        private void monitorChildren(Parent p, Monitor monitor, ListChangeListener<String> styleSheetChangeListener) {
            monitorStylesheets(p.getStylesheets(), monitor.uriToFileCreators, styleSheetChangeListener);

            List<Node> actualChildren = new LinkedList<Node>(p.getChildrenUnmodifiable());
            for (Node child : actualChildren) {
                if (child instanceof Parent) {
                    Parent childAsParent = (Parent) child;
                    monitorParent(childAsParent, monitor, styleSheetChangeListener);
                }
            }

            p.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
                @Override
                public void onChanged(javafx.collections.ListChangeListener.Change<? extends Node> c) {
                    while (c.next()) {
                        if (c.wasRemoved()) {
                            // todo un-monitor
                        }
                        if (c.wasAdded()) {
                            for (Node addedNode : c.getAddedSubList()) {
                                if (addedNode instanceof Parent) {
                                    Parent addedParent = (Parent) addedNode;
                                    monitorChildren(addedParent, monitor, styleSheetChangeListener);
                                }
                            }
                        }
                    }
                }
            });
        }

        private void registerURI(String uri, ObservableList<String> stylesheets, List<Function<String, Path>> uriToFileCreators) {
            if (!knownURIs.contains(uri)) {
                for (Function<String, Path> uriToPath : uriToFileCreators) {
                    Path sourceFile = uriToPath.apply(uri);
                    if (sourceFile != null) {
                        Path directory = sourceFile.getParent();
                        pw.monitor(
                                directory.toAbsolutePath().normalize()
                                , sourceFile.toAbsolutePath().normalize()
                                , new URIStyleUpdater(uri, sourceFile.toUri().toString(), stylesheets)
                        );
                        knownURIs.add(sourceFile.toUri().toString());
                        return;
                    }
                }
            }
        }

        public ObservableList<Monitor> monitors() {
            return monitors;
        }
    }

    // jar:file/(.*)/target/(.*).jar!/(.*).css
    // jar:file:/D:/dev/projects/perso/javafx/testapp/target/testapp-1.0-SNAPSHOT-shaded.jar!/org/fxmisc/cssfx/test/app.css

    private static Pattern[] JAR_PATTERNS = {
            Pattern.compile("jar:file:/(.*)/target/(.*)\\.jar!/(.*\\.css)") // resource from maven jar in target directory
            , Pattern.compile("jar:file:/(.*)/build/(.*)\\.jar!/(.*\\.css)") // resource from gradle jar in target directory
    };
    private static String[] JAR_SOURCES_REPLACEMENTS = {
            "src/main/java", "src/main/resources", "src/test/java", "src/test/resources" };

    private static Path jarResourceFileFromURI(String uri) {
        String sourceFileURIPattern = "file:/%s/%s/%s";
        for (Pattern jp : JAR_PATTERNS) {
            Matcher m = jp.matcher(uri);
            if (m.matches()) {
                for (String string : JAR_SOURCES_REPLACEMENTS) {
                    String potentialSourceURI = String.format(sourceFileURIPattern, m.group(1), string, m.group(3));
                    try {
                        Path p = Paths.get(new URI(potentialSourceURI));
                        if (Files.exists(p)) {
                            return p;
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private static Path gradleResourceFileFromURI(String uri) {
        if (uri != null && uri.startsWith("file:")) {
            if (uri.contains("build/classes/main")) {
                String[] classesTransform = {
                        "src/main/java", "src/main/resources" };
                for (String ct : classesTransform) {
                    String potentialSourceURI = uri.replace("target/classes", ct);
                    try {
                        Path p = Paths.get(new URI(potentialSourceURI));
                        if (Files.exists(p)) {
                            return p;
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            } else if (uri.contains("build/classes/test")) {
                String[] testClassesTransform = {
                        "src/test/java", "src/test/resources" };
                for (String tct : testClassesTransform) {
                    String potentialSourceURI = uri.replace("target/test-classes", tct);
                    try {
                        Path p = Paths.get(new URI(potentialSourceURI));
                        if (Files.exists(p)) {
                            return p;
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return null;
    }

    private static Path mavenResourceFileFromURI(String uri) {
        if (uri != null && uri.startsWith("file:")) {
            if (uri.contains("target/classes")) {
                String[] classesTransform = {
                        "src/main/java", "src/main/resources" };
                for (String ct : classesTransform) {
                    String potentialSourceURI = uri.replace("target/classes", ct);
                    try {
                        Path p = Paths.get(new URI(potentialSourceURI));
                        if (Files.exists(p)) {
                            return p;
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            } else if (uri.contains("target/test-classes")) {
                String[] testClassesTransform = {
                        "src/test/java", "src/test/resources" };
                for (String tct : testClassesTransform) {
                    String potentialSourceURI = uri.replace("target/test-classes", tct);
                    try {
                        Path p = Paths.get(new URI(potentialSourceURI));
                        if (Files.exists(p)) {
                            return p;
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return null;
    }

    private static class URIStyleUpdater implements Runnable {
        private final String sourceURI;
        private final String originalURI;
        private final ObservableList<String> cssURIs;

        URIStyleUpdater(String originalURI, String sourceURI, ObservableList<String> cssURIs) {
            this.originalURI = originalURI;
            this.sourceURI = sourceURI;
            this.cssURIs = cssURIs;
        }

        @Override
        public void run() {
            Platform.runLater(() -> {
                cssURIs.remove(originalURI);
                cssURIs.remove(sourceURI);
            });
            Platform.runLater(() -> {
                cssURIs.add(sourceURI);
            });
        }
    }

    @FunctionalInterface
    public static interface Stoppable {
        public void stop();
    }
}
