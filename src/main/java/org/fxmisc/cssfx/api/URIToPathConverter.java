package org.fxmisc.cssfx.api;

import java.nio.file.Path;

/**
 * An URIToPathConverter is used to associate a resource file from the classpath to a physical file on disk.
 * 
 * @author Matthieu Brouillard
 */
@FunctionalInterface
public interface URIToPathConverter {
    public Path convert(String uri);
}
