package com.tannazetm.dailytasktracker.util;

import androidx.annotation.Nullable;

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * Prevents re-triggering of events after configuration changes (like tab switching).
 */
public class Event<T> {
    private T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Returns the content and prevents its use again.
     */
    @Nullable
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    public T peekContent() {
        return content;
    }

    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }
}