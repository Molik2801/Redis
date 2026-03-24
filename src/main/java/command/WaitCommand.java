package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class WaitCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
       out.write(":0\r\n".getBytes());
       out.flush();
    }
    
}
