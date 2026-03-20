package store;

import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedisStore {
    public  Integer milisecondsTime = 0;
    public  Integer sequenceNumber = 0;
    public  final ConcurrentHashMap<String , RedisData> data = new ConcurrentHashMap<>();
    public  final ConcurrentHashMap<String , ArrayDeque<String>> list = new ConcurrentHashMap<>();
    public  final ConcurrentHashMap<String , ConcurrentHashMap<String , String>> stream = new ConcurrentHashMap<>();
    public  ConcurrentHashMap<String, ConcurrentLinkedQueue<CompletableFuture<String>>> waiters = new ConcurrentHashMap<>();
}
