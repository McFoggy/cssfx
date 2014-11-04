package org.fxmisc.cssfx.paths;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

public class PathsWatcher {
	private WatchService watchService;
	private Map<String, Map<String, Runnable>> filesActions = new HashMap<>();
	private Thread watcherThread;

	public PathsWatcher() {
		try {
			watchService = FileSystems.getDefault().newWatchService();

		} catch (IOException e) {
			System.err.println("cannot monitor file system, " + e.getMessage());
		}
	}

	public void monitor(Path directory, Path sourceFile, Runnable action) {
		if (watchService != null) {
			Map<String, Runnable> fileAction = filesActions.computeIfAbsent(
					directory.toString(), (p) -> {
						try {
							System.out.println("monitoring directory: " + p);
							directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return new HashMap<>();
					});

			fileAction.put(sourceFile.toString(), action);
		}
	}

	public void watch() {
		watcherThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					WatchKey key;
					try {
						key = watchService.take();
					} catch (InterruptedException ex) {
						return;
					}

					for (WatchEvent<?> event : key.pollEvents()) {
						WatchEvent.Kind<?> kind = event.kind();

						System.out.println("event occured: " + kind);
						if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
							// it is a modification
							@SuppressWarnings("unchecked")
							WatchEvent<Path> ev = (WatchEvent<Path>) event;
							Path directory = ((Path)key.watchable()).toAbsolutePath().normalize();
							Path modifiedFile = directory.resolve(ev.context()).toAbsolutePath().normalize();
							
							System.out.println("modified file: " + modifiedFile);
							if (filesActions.containsKey(directory.toString())) {
								Map<String, Runnable> filesAction = filesActions.get(directory.toString());
								if (filesAction.containsKey(modifiedFile.toString())) {
									Runnable action = filesAction.get(modifiedFile.toString());
									action.run();
								}
							}
						}
					}

					boolean valid = key.reset();
					if (!valid) {
						break;
					}
				}
			}
		}, "CSSFX-file-monitor");
		watcherThread.start();
	}

	public void stop() {
		watcherThread.interrupt();
	}
}
