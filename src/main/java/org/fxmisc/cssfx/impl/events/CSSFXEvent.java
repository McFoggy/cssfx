package org.fxmisc.cssfx.impl.events;

public final class CSSFXEvent<T> {
    private final EventType eventType;
    private final T eventData;
    
    public static enum EventType {
        STYLESHEET_ADDED
        , STYLESHEET_REMOVED
        , NODE_ADDED
        , NODE_REMOVED
        , SCENE_ADDED
        , SCENE_REMOVED
        , STAGE_ADDED
        , STAGE_REMOVED
    }
    
    private  CSSFXEvent(EventType type, T data) {
        eventType = type;
        eventData = data;
    }

    public EventType getEventType() {
        return eventType;
    }

    public T getEventData() {
        return eventData;
    }
    
    public static <T> CSSFXEvent<T> newEvent(EventType type, T data) {
        return new CSSFXEvent<T>(type, data);
    }

    @Override
    public String toString() {
        return String.format("CSSFXEvent [eventType=%s, eventData=%s]", eventType, eventData);
    }
}
