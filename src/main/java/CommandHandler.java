import java.io.*;
import java.lang.reflect.Array;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandHandler {

    static void commandResponse(List<String> input , OutputStream outputStream) throws IOException {
        if(input == null || input.isEmpty()){
            return;
        }
        if(input.get(0).equals("PING")){
            outputStream.write("+PONG\r\n".getBytes());
        }
        else if(input.get(0).equalsIgnoreCase("ECHO")){
            String res = input.get(1);
            outputStream.write(("$" + res.length() + "\r\n" + res + "\r\n").getBytes());
        }
        else if(input.get(0).equals("SET")){
            RedisData data = new RedisData();
            data.value = input.get(2);
            if(input.size() >= 5){
                if(Objects.equals(input.get(3), "EX")){
                    data.expiryTime = System.currentTimeMillis() + Integer.parseInt(input.get(4))* 100L;
                }
                else if(Objects.equals(input.get(3), "PX")){
                    data.expiryTime = System.currentTimeMillis() + Integer.parseInt(input.get(4));
                }
            }
            GlobalMaps.data.put(input.get(1) , data);
            outputStream.write("+OK\r\n".getBytes());
        }
        else if(input.get(0).equals("GET")){
            RedisData res = GlobalMaps.data.get(input.get(1));
            long curTime = System.currentTimeMillis();
            if(curTime > res.expiryTime){
                outputStream.write("$-1\r\n".getBytes());
                GlobalMaps.data.remove(input.get(1));
            }
            else {
                outputStream.write(("$" + res.value.length() + "\r\n" + res.value + "\r\n").getBytes());
            }
        }
        else if(input.get(0).equals("RPUSH")){

                String key = input.get(1);

                int currentSize = GlobalMaps.list.containsKey(key) ? GlobalMaps.list.get(key).size() : 0;
                int pushedCount = input.size() - 2; 
                int expectedRedisSize = currentSize + pushedCount;
                outputStream.write((":" + expectedRedisSize + "\r\n").getBytes());
                
                GlobalMaps.waiters.putIfAbsent(key , new ConcurrentLinkedQueue<>());
                ConcurrentLinkedQueue<CompletableFuture<String>> queue = GlobalMaps.waiters.get(key);

                for(int i = 2 ; i < input.size() ; i++){

                    String element = input.get(i);
                    
                    synchronized(queue){
                        if(queue != null && !queue.isEmpty()){
                            CompletableFuture<String> future = queue.poll();
                            if(future != null){
                                future.complete(element);
                                continue;
                            }
                        }
                        else{
                            GlobalMaps.list.putIfAbsent(key , new ArrayDeque<>());
                            GlobalMaps.list.get(key).addLast(element);
                        }
                    }
                }
        }
        else if(input.get(0).equals("LRANGE")){
            int l = Integer.parseInt(input.get(2));
            int r = Integer.parseInt(input.get(3));
            String listName = input.get(1);
            StringBuilder respBulk = new StringBuilder();

            if(GlobalMaps.list.containsKey(listName)){
                if(l < 0){
                    l = Math.max(0 , GlobalMaps.list.get(listName).size() + l);
                }
                if(r < 0){
                    r = Math.max(0 , GlobalMaps.list.get(listName).size() + r);
                }
                int size = Math.min(r , GlobalMaps.list.get(listName).size() - 1) - Math.max(0 , l) + 1;
                respBulk.append("*").append(size).append("\r\n");
                System.out.println(size);
                int lr = Math.max(0 , l);
                int rr = Math.min(r , GlobalMaps.list.get(listName).size() - 1);
                List<String> range = GlobalMaps.list.get(listName).stream().skip(lr).limit(rr - lr + 1).toList();
                System.out.println(range);
                for(int i = 0 ; i < range.size() ; i++){
                    String element = range.get(i);
                    System.out.println(i + " " + element);
                    respBulk.append("$").append(element.length()).append("\r\n").append(element).append("\r\n");
                }
            }
            else{
                respBulk.append("*0\r\n");
            }
            outputStream.write(respBulk.toString().getBytes());
        }
        else if(input.get(0).equals("LPUSH")){
            String key = input.get(1);

            int currentSize = GlobalMaps.list.containsKey(key) ? GlobalMaps.list.get(key).size() : 0;
            int pushedCount = input.size() - 2; 
            int expectedRedisSize = currentSize + pushedCount;
            outputStream.write((":" + expectedRedisSize + "\r\n").getBytes());

             GlobalMaps.waiters.putIfAbsent(key , new ConcurrentLinkedQueue<>());
                ConcurrentLinkedQueue<CompletableFuture<String>> queue = GlobalMaps.waiters.get(key);

                for(int i = 2 ; i < input.size() ; i++){

                    String element = input.get(i);
                    
                    synchronized(queue){
                        if(queue != null && !queue.isEmpty()){
                            CompletableFuture<String> future = queue.poll();
                            if(future != null){
                                future.complete(element);
                                continue;
                            }
                        }
                        else{
                            GlobalMaps.list.putIfAbsent(key , new ArrayDeque<>());
                            GlobalMaps.list.get(key).addFirst(element);
                        }
                    }
                }
        }
        else if(input.get(0).equals("LLEN")){
            int size = 0;
            if(GlobalMaps.list.containsKey(input.get(1))){
                size = GlobalMaps.list.get(input.get(1)).size();
            }
            outputStream.write((":" + size + "\r\n").getBytes());
        }
        else if (input.get(0).equals("LPOP")) {
            String key = input.get(1);
            Deque<String> deque = GlobalMaps.list.get(key);

            if (deque == null || deque.isEmpty()) {
                outputStream.write("$-1\r\n".getBytes());
                return;
            }

            int totalToRemove = 1;
            boolean hasCount = input.size() > 2;

            if (hasCount) {
                try {
                    totalToRemove = Math.min(Integer.parseInt(input.get(2)), deque.size());
                } catch (NumberFormatException e) {
                    outputStream.write("-ERR value is not an integer\r\n".getBytes());
                    return;
                }
            }

            StringBuilder resp = new StringBuilder();
            if (hasCount) {
                resp.append("*").append(totalToRemove).append("\r\n");
            }
            for (int i = 0; i < totalToRemove; i++) {
                String element = deque.removeFirst();
                resp.append("$").append(element.length()).append("\r\n").append(element).append("\r\n");
            }
            outputStream.write(resp.toString().getBytes());
        }
        else if(input.get(0).equals("BLPOP")){
            String key = input.get(1);

            // Ensure a queue exists so we have an object to lock onto
            GlobalMaps.waiters.putIfAbsent(key, new ConcurrentLinkedQueue<>());
            ConcurrentLinkedQueue<CompletableFuture<String>> queue = GlobalMaps.waiters.get(key);
            
            CompletableFuture<String> future = new CompletableFuture<>();
            boolean immediatePop = false;
            String poppedElement = null;

            // SYNCHRONIZE to prevent RPUSH from happening while we check
            synchronized (queue) {
                Deque<String> deque = GlobalMaps.list.get(key);
                
                if (deque != null && !deque.isEmpty()) {
                    // Data is already here! Grab it.
                    poppedElement = deque.removeFirst();
                    immediatePop = true;
                } else {
                    // No data. Safely add ourselves to the wait queue while locked.
                    queue.add(future);
                }
            }

            try {
                String element;
                if (immediatePop) {
                    element = poppedElement;
                } else {
                    // IMPORTANT: We call future.get() OUTSIDE the synchronized block!
                    // If we waited inside, RPUSH would never be able to get the lock to wake us up.
                    element = future.get(); 
                }

                String response = "*2\r\n$" + key.length() + "\r\n" + key + "\r\n$" + element.length() + "\r\n" + element + "\r\n";
                outputStream.write(response.getBytes());
                outputStream.flush(); // Always good practice to flush the stream!

            } catch (Exception e) {
                outputStream.write("-ERR BLPOP interrupted\r\n".getBytes());
                Thread.currentThread().interrupt();
            }
        }
    }
}
