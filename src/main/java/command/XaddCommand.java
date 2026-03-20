package command;

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import store.RedisStore;
import store.RedisStream;

public class XaddCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        String id = input.get(2);
    
        store.streams.putIfAbsent(key , new RedisStream());
        RedisStream stream = store.streams.get(key);

        String generatedId;
        Map<String , String> entry = new LinkedHashMap<>();

        synchronized(stream){
            long newTime;
            int newSeq;
            
            if(id.equals("*")){
                newTime = System.currentTimeMillis();
                if(newTime <= stream.lastTime){
                    newTime = stream.lastTime;
                    newSeq = stream.lastSeq + 1;
                }
                else{
                    newSeq = 0;
                }
            }
            else{
                long curTime = Long.parseLong(id.split("-")[0]);
                String curSeq = id.split("-")[1];

                if(curSeq.equals("*")){
                    newTime = curTime;
                    if(newTime == stream.lastTime){
                        newSeq = stream.lastSeq + 1;
                    }
                    else if(newTime > stream.lastTime){
                        newSeq = (newTime == 0) ? 1 : 0;
                    }
                    else{
                        out.write("-ERR The ID specified in XADD is equal or smaller than the target stream top item\r\n".getBytes());
                        out.flush();
                        return;
                    }
                }
                else{
                    newTime = curTime;
                    newSeq = Integer.parseInt(curSeq);

                    if(newTime == 0 && newSeq == 0){
                        out.write("-ERR The ID specified in XADD must be greater than 0-0\r\n".getBytes());
                        out.flush();
                        return;
                    }
                    if(newTime < stream.lastTime || (newTime == stream.lastTime && newSeq <= stream.lastSeq)){
                        out.write("-ERR The ID specified in XADD is equal or smaller than the target stream top item\r\n".getBytes());
                        out.flush();
                        return;
                    }
                }
            }
            stream.lastTime = newTime;
            stream.lastSeq = newSeq;

            generatedId = newTime + "-" + newSeq;

            for(int i = 3 ; i < input.size() ; i+=2){
                entry.put(input.get(i) , input.get(i+1));
            }

            stream.entries.put(generatedId , entry);
        }

        out.write(("$" + generatedId.length() + "\r\n" + generatedId + "\r\n").getBytes());
        out.flush();
    }
    
}
