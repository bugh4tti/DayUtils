package com.dayutils.plugin.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class NightVisionManager {

    private final Set<UUID> enabled = Collections.synchronizedSet(new HashSet<>());

    public boolean isEnabled(UUID uuid) {
        return enabled.contains(uuid);
    }

    public void enable(UUID uuid) {
        enabled.add(uuid);
    }

    public void disable(UUID uuid) {
        enabled.remove(uuid);
    }
}
