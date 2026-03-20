package store;

import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedisStore {
    public static final ConcurrentHashMap<String , RedisData> data = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String , ArrayDeque<String>> list = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<CompletableFuture<String>>> waiters = new ConcurrentHashMap<>();
}
