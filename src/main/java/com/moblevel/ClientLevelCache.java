package com.moblevel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Client-side entityId -> level map, fed by LevelSyncPayload. Entries are
// removed as entities leave the client level and cleared on disconnect
// (see ClientRenderEvents), so it never outgrows the set of tracked entities.
public final class ClientLevelCache {
    private static final Map<Integer, Integer> LEVELS = new ConcurrentHashMap<>();

    private ClientLevelCache() {
    }

    public static void put(int entityId, int level) {
        LEVELS.put(entityId, level);
    }

    public static Integer get(int entityId) {
        return LEVELS.get(entityId);
    }

    public static void remove(int entityId) {
        LEVELS.remove(entityId);
    }

    public static void clear() {
        LEVELS.clear();
    }
}
