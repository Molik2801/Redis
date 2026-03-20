package command;

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import store.RedisStore;

public class LPushCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        int currentSize = store.list.containsKey(key) ? store.list.get(key).size() : 0;
        int pushedCount = input.size() - 2; 
        int expectedRedisSize = currentSize + pushedCount;
        out.write((":" + expectedRedisSize + "\r\n").getBytes());
            store.waiters.putIfAbsent(key , new ConcurrentLinkedQueue<>());
            ConcurrentLinkedQueue<CompletableFuture<String>> queue = store.waiters.get(key);

            for(int i = 2 ; i < input.size() ; i++){

                String element = input.get(i);
                
                synchronized(queue){
                    boolean handedOff = false;
                
                    // Keep pulling from the queue until we find a living, waiting client
                    while (!queue.isEmpty()) {
                        CompletableFuture<String> future = queue.poll();
                        
                        // Try to hand it off. If complete() returns true, it worked!
                        if (future.complete(element)) {
                            handedOff = true;
                            break; 
                        }
                        // If it returns false, the future was dead (timed out). The loop continues.
                    }
                    
                    // If no one was waiting (or all waiters had timed out), add it to the list
                    if (!handedOff) {
                        store.list.putIfAbsent(key, new ArrayDeque<>());
                        store.list.get(key).addFirst(element);
                    }
                }
            }
    }
    
}
