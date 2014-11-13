package org.fxmisc.cssfx.impl;

import org.fxmisc.cssfx.impl.events.CSSFXEvent;

public interface CSSFXEventNotifer {

    public abstract void eventNotify(CSSFXEvent<?> e);

}