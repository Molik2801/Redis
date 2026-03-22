package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class ReplConfCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
       out.write("+OK\r\n".getBytes());
    }
    
}
