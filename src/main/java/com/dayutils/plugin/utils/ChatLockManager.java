package com.dayutils.plugin.utils;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ChatLockManager {

    private final AtomicBoolean locked = new AtomicBoolean(false);

    public boolean isLocked() {
        return locked.get();
    }

    public void setLocked(boolean locked) {
        this.locked.set(locked);
    }
}
