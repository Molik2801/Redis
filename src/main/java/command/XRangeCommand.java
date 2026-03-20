package command;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import store.RedisStore;
import store.RedisStream;

public class XRangeCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
       String key = input.get(1);
       String start = input.get(2);
       String end = input.get(3);

       RedisStream stream = store.streams.get(key);

       if(start.equals("-")){
          start = "0-0";
       }
    //    System.out.println(start + end);
    //    System.out.println(stream.entries);
       SortedMap<String, Map<String, String>> subMap = stream.entries.subMap(start , true , end , true);
    //    System.out.println(subMap);

       StringBuilder res = new StringBuilder();

       Iterator<Map.Entry<String , Map<String , String>>> it = subMap.entrySet().iterator();
       
       res.append("*" + subMap.size() + "\r\n");
       while(it.hasNext()){
            Map.Entry<String , Map<String , String>> entry = it.next();
            res.append("*2\r\n");
            res.append("$" + entry.getKey().length() + "\r\n" + entry.getKey() + "\r\n");

            Map<String , String> remValues = entry.getValue();
            res.append("*" + remValues.size() * 2 + "\r\n");
            for(Map.Entry<String , String> remEntry : remValues.entrySet()){
                res.append("$" + remEntry.getKey().length() + "\r\n" + remEntry.getKey() + "\r\n");
                res.append("$" + remEntry.getValue().length() + "\r\n" + remEntry.getValue() + "\r\n");
            }
       }

       out.write(res.toString().getBytes());
       out.flush();
    }
        
}
