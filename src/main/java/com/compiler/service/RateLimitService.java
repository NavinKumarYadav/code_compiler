package com.compiler.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();

    private final int MAX_REQUESTS_PER_MINUTE = 100;

    public boolean isAllowed(String userIdentifier){
        String key = userIdentifier + ":" + (System.currentTimeMillis()/ 60000);

        AtomicInteger count = requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));

        if(count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE){
            return false;
        }

        cleanupOldEntries();

        return true;
    }

    private void cleanupOldEntries(){
        long currentMinute = System.currentTimeMillis() / 60000;
        requestCounts.keySet().removeIf(key -> {
            long keyMinute = Long.parseLong(key.split(":")[1]);
            return currentMinute - keyMinute > 1;
        });
    }

    public int getRemainingRequests(String clientId){
        String key = clientId + ":" + (System.currentTimeMillis() / 60000);
        AtomicInteger count = requestCounts.get(key);
        return count != null ? Math.max(0, MAX_REQUESTS_PER_MINUTE - count.get()) : MAX_REQUESTS_PER_MINUTE;
    }
}