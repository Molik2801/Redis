package command;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import store.RedisStore;
import store.RedisStream;

public class XReadCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {

        if(input.get(1).toUpperCase().equals("BLOCK")){
            blockRead(input , out , store);
            return;
        }

        int streamNum = (input.size() - 2) / 2;

        StringBuilder res = new StringBuilder();
        res.append("*" + streamNum + "\r\n");


        for(int i = 2 ; i < input.size() - streamNum ; i++){
            String key = input.get(i);

            res.append("*2\r\n");
            res.append("$" + key.length() + "\r\n" + key + "\r\n");
            RedisStream stream = store.streams.get(key);

            // System.out.println(id);
            String start = input.get(i + streamNum);
            String end = String.valueOf(Long.MAX_VALUE) + "-" + String.valueOf(Long.MAX_VALUE);  
            SortedMap<String , Map<String , String>> subMap = stream.entries.subMap(start , false , end , true);

            res.append("*" + subMap.size() + "\r\n");
            
            for(Map.Entry<String , Map<String , String>> entry1 : subMap.entrySet()){
                res.append("*2\r\n");
                res.append("$" + entry1.getKey().length() + "\r\n" + entry1.getKey() + "\r\n");

                Map<String , String> value1 = entry1.getValue();
                res.append("*" + value1.size() * 2 + "\r\n");
                for(Map.Entry<String , String> entry2 : value1.entrySet()){
                    res.append("$" + entry2.getKey().length() + "\r\n" + entry2.getKey() + "\r\n");
                    res.append("$" + entry2.getValue().length() + "\r\n" + entry2.getValue() + "\r\n");
                } 
            }
        }

        out.write(res.toString().getBytes());
        out.flush();
    } 
    
    public void blockRead(List<String> input , OutputStream out , RedisStore store) throws Exception{
        long blockTime = Long.parseLong(input.get(2));
        String key = input.get(4);
        String id = input.get(5);

        store.streamWaiters.putIfAbsent(key , new ConcurrentLinkedQueue<>());
        ConcurrentLinkedQueue<CompletableFuture<String>> queue = store.streamWaiters.get(key);

        CompletableFuture<String> future = new CompletableFuture<>();

        boolean available = false;

        RedisStream stream = store.streams.get(key);
        if((stream != null) && (!id.equals("$"))){
            SortedMap<String , Map<String , String>> subMap = stream.entries.subMap(id , false , String.valueOf(Long.MAX_VALUE) + "-" + String.valueOf(Long.MAX_VALUE) , true);
            if(subMap.isEmpty()){
                queue.add(future);
            }
            else{
                available = true;
            }
        }
        else{
            queue.add(future);
        }
                    
        try {
            if(available){
                List<String> newList = new ArrayList<>(input);
                newList.subList(1 , 3).clear();
                execute(newList , out , store);
            }
            else{
                String futureId;
                if(blockTime == 0) futureId = future.get();
                else futureId = future.get(blockTime , TimeUnit.MILLISECONDS);
                StringBuilder newRes = new StringBuilder();
                newRes.append("*1\r\n");
                newRes.append("*2\r\n");
                newRes.append("$" + key.length() + "\r\n" + key + "\r\n");
                RedisStream fstream = store.streams.get(key);
                Map<String , String> futureEntry = fstream.entries.get(futureId);
                newRes.append("*1\r\n");
                newRes.append("*2\r\n");
                newRes.append("$" + futureId.length() + "\r\n" + futureId + "\r\n");
                newRes.append("*" + futureEntry.size() * 2 + "\r\n");
                for(Map.Entry<String , String> entry : futureEntry.entrySet()){
                    newRes.append("$" + entry.getKey().length() + "\r\n" + entry.getKey() + "\r\n");
                    newRes.append("$" + entry.getValue().length() + "\r\n" + entry.getValue() + "\r\n");
                }
                out.write(newRes.toString().getBytes());
                out.flush();
            }
        } catch (TimeoutException e){
            future.cancel(false);
            synchronized(queue){
                queue.remove(future);
            }
            out.write("*-1\r\n".getBytes());
            out.flush();
        } catch (Exception e) {
            out.write("-ERR BLOCK interrupted\r\n".getBytes());
            Thread.currentThread().interrupt();
        }
    }
    
}
