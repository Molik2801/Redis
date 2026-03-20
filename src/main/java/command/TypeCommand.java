package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class TypeCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
       String key = input.get(1);
       String type = "none";
       if(store.streams.containsKey(key)){
            type = "stream";
       }
       else if(store.data.containsKey(key)){
            type = "string";
       }

       out.write(("+" + type + "\r\n").getBytes());
       out.flush();
    }   
}
