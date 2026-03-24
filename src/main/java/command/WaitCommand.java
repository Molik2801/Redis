package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class WaitCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
       int replicaCount = store.replicaOutputStreams.size();
        out.write((":" + replicaCount + "\r\n").getBytes());
       out.flush();
    }
    
}
