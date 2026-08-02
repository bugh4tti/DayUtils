package com.dayutils.plugin.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Guarda en memoria la última vez que cada jugador se desconectó
 * durante esta sesión del servidor.
 */
public final class SeenManager {

    private final Map<UUID, Long> lastQuit = Collections.synchronizedMap(new HashMap<>());

    public void markQuit(UUID uuid) {
        lastQuit.put(uuid, System.currentTimeMillis());
    }

    public Long getLastQuit(UUID uuid) {
        return lastQuit.get(uuid);
    }
}
