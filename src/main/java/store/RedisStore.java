package store;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class RedisStore {

    public boolean isSlave = false;
    public String masterHost = null;
    public int masterPort = 6379;
    public int ackOffset = 0;
    public int masterOffset = 0;

    public  final ConcurrentHashMap<String , RedisData> data = new ConcurrentHashMap<>();
    
    public  final ConcurrentHashMap<String , ArrayDeque<String>> list = new ConcurrentHashMap<>();
    public  final ConcurrentHashMap<String , RedisStream> streams = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String , ConcurrentLinkedQueue<CompletableFuture<String>>> streamWaiters = new ConcurrentHashMap<>();
    public  ConcurrentHashMap<String, ConcurrentLinkedQueue<CompletableFuture<String>>> waiters = new ConcurrentHashMap<>();

    //List for slave replication storing outputstreams of all replicas
    public List<ReplicaConnection> replicaOutputStreams = new CopyOnWriteArrayList<>();

    //broadcasting to all replicas
    public synchronized void broadcastToReplicas(List<String> input){

        StringBuilder replCommand = new StringBuilder();
        replCommand.append("*" + input.size() + "\r\n");

        for(String str : input){
            replCommand.append("$" + str.length() + "\r\n" + str + "\r\n");
        }

        this.masterOffset += replCommand.length();

        for(ReplicaConnection replOut: replicaOutputStreams){
            try {
                replOut.outputStream.write(replCommand.toString().getBytes());
                replOut.outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
