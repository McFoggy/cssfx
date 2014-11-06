package org.fxmisc.cssfx.impl.events;

@FunctionalInterface
public interface CSSFXEventListener {
    public void onEvent(CSSFXEvent<?> event);
}
