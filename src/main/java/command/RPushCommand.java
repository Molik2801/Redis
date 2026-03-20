package command;

import store.RedisStore;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RPushCommand implements Command {
    @Override
    public void execute(List<String> input, OutputStream outputStream, RedisStore store) throws Exception {
        String key = input.get(1);

        int currentSize = store.list.containsKey(key) ? store.list.get(key).size() : 0;
        int pushedCount = input.size() - 2; 
        int expectedRedisSize = currentSize + pushedCount;
        outputStream.write((":" + expectedRedisSize + "\r\n").getBytes());
        
        store.waiters.putIfAbsent(key, new ConcurrentLinkedQueue<>());
        ConcurrentLinkedQueue<CompletableFuture<String>> queue = store.waiters.get(key);

        for(int i = 2; i < input.size(); i++){
            String element = input.get(i);
            
            synchronized(queue){
                boolean handedOff = false;
                while (!queue.isEmpty()) {
                    CompletableFuture<String> future = queue.poll();
                    if (future.complete(element)) {
                        handedOff = true;
                        break; 
                    }
                }
                
                if (!handedOff) {
                    store.list.putIfAbsent(key, new ArrayDeque<>());
                    store.list.get(key).addLast(element);
                }
            }
        }
    }
}