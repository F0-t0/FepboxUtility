package com.fepbox.utility.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownService {
    private final Map<String, Long> map = new ConcurrentHashMap<>();

    private String key(UUID id, String action){ return id+":"+action; }

    public boolean isOnCooldown(UUID id, String action){
        return remaining(id, action) > 0;
    }

    public long remaining(UUID id, String action){
        return Math.max(0, map.getOrDefault(key(id, action), 0L) - Instant.now().getEpochSecond());
    }

    public void put(UUID id, String action, int seconds){
        if (seconds <=0) { map.remove(key(id, action)); return; }
        map.put(key(id, action), Instant.now().getEpochSecond() + seconds);
    }
}
