package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class LLenCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
       int size = 0;
        if(store.list.containsKey(input.get(1))){
            size = store.list.get(input.get(1)).size();
        }
        // System.out.println(size);
        out.write((":" + size + "\r\n").getBytes());
    }
    
}
