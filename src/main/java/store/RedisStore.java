package store;

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedisStore {

    public boolean isSlave = false;
    public String masterHost = null;
    public int masterPort = 6379;

    public  final ConcurrentHashMap<String , RedisData> data = new ConcurrentHashMap<>();
    
    public  final ConcurrentHashMap<String , ArrayDeque<String>> list = new ConcurrentHashMap<>();
    public  final ConcurrentHashMap<String , RedisStream> streams = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String , ConcurrentLinkedQueue<CompletableFuture<String>>> streamWaiters = new ConcurrentHashMap<>();
    public  ConcurrentHashMap<String, ConcurrentLinkedQueue<CompletableFuture<String>>> waiters = new ConcurrentHashMap<>();

    //List for slave replication storing outputstreams of all replicas
    public List<OutputStream> replicaOutputStreams = new ArrayList<>();
}
