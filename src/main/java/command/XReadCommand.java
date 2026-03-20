package command;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import store.RedisStore;
import store.RedisStream;

public class XReadCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {

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
}
