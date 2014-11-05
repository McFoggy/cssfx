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
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
		private List<Function<String, Path>> uriToFileCreators = new LinkedList<Function<String,Path>>();

		private ObservableList<String> stylesheets;

		private Monitor(Monitors chain) {
			this.monitors = chain;
			uriToFileCreators.add(CSSFX::mavenResourceFileFromURI);
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
		
		private Monitors() {
		}
		
		public Stoppable start() {
			PathsWatcher pw = new PathsWatcher();
			
			for (Monitor monitor : monitors) {
				for (String uri : monitor.stylesheets()) {
					for (Function<String, Path> uriToPath : monitor.uriToFileCreators) {
						Path sourceFile = uriToPath.apply(uri);
						if (sourceFile != null) {
							Path directory = sourceFile.getParent();
							pw.monitor(directory.toAbsolutePath().normalize(), sourceFile.toAbsolutePath().normalize(), new URIStyleUpdater(uri, sourceFile.toUri().toString(), monitor.stylesheets()));
							continue;
						}
					}
				}
			}
			
			pw.watch();
			return pw::stop;
		}
		
		public ObservableList<Monitor> monitors() {
			return monitors;
		}
	}
	
	private static Path mavenResourceFileFromURI(String uri) {
		if (uri != null && uri.startsWith("file:")) {
			if (uri.contains("target/classes")) {
				String[] classesTransform = {"src/main/java", "src/main/resources"};
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
				String[] testClassesTransform = {"src/test/java", "src/test/resources"};
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
			
			return null;
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
			System.out.println("change detected on : " + sourceURI);
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
