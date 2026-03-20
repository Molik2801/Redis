package command;

import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import store.RedisStore;

public class XaddCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        String id = input.get(2);
    
        
        if(id.equals("*")){
            long sysTime = System.currentTimeMillis();
            if(sysTime == store.milisecondsTime){
                store.sequenceNumber += 1;
            }
            else{
                store.milisecondsTime = sysTime;
                store.sequenceNumber = 0;
            }
        }
        else{
            String curTime = id.split("-")[0];
            String curSeq = id.split("-")[1];
            if(curSeq.equals("*")){
                long time = Long.parseLong(curTime);
                if(time == store.milisecondsTime){
                    store.sequenceNumber += 1;
                }
                else if(time > store.milisecondsTime){
                    store.milisecondsTime = time;
                    store.sequenceNumber = 0;
                }
                else{
                    out.write("-ERR The ID specified in XADD is equal or smaller than the target stream top item\r\n".getBytes());
                    out.flush();
                    return;
                }
            }
            else{
                long time = Long.parseLong(curTime);
                int seq = Integer.parseInt(curSeq);
                if(time == 0 && seq == 0){
                    out.write("-ERR The ID specified in XADD must be greater than 0-0\r\n".getBytes());
                    out.flush();
                    return;
                }
                else if(time < store.milisecondsTime || (time == store.milisecondsTime && seq <= store.sequenceNumber)){
                    out.write("-ERR The ID specified in XADD is equal or smaller than the target stream top item\r\n".getBytes());
                    out.flush();
                    return;
                }
                else{
                    store.milisecondsTime = time;
                    store.sequenceNumber = seq;
                }
            }
        }

        id = store.milisecondsTime + "-" + store.sequenceNumber;
        ConcurrentHashMap<String , String> entries = new ConcurrentHashMap<>();
        entries.put("id", id);
        for(int i = 3 ; i < input.size() ; i+=2){
            entries.put(input.get(i) , input.get(i+1));
        }
        store.stream.putIfAbsent(key , entries);
        out.write(("$" + id.length() + "\r\n" + id + "\r\n").getBytes());
        out.flush();
    }
    
}
