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

        int curTime = Integer.parseInt(id.split("-")[0]);
        int curSeq = Integer.parseInt(id.split("-")[1]);
        
        if(curTime == 0 && curSeq == 0){
            out.write("-ERR The ID specified in XADD must be greater than 0-0\r\n".getBytes());
            out.flush();
            return;
        }
        else if(curTime < store.milisecondsTime || (curTime == store.milisecondsTime && curSeq <= store.sequenceNumber)){
            out.write("-ERR The ID specified in XADD is equal or smaller than the target stream top item\r\n".getBytes());
            out.flush();
            return;
        }
        else{
            store.milisecondsTime = curTime;
            store.sequenceNumber = curSeq;
        }

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
