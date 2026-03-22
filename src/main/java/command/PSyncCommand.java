package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisRDBContent;
import store.RedisStore;

public class PSyncCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        if(input.get(1).equals("?")){
            out.write("+FULLRESYNC 8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb 0\r\n".getBytes());
            out.write(("$" + RedisRDBContent.RDBFile.length + "\r\n").getBytes());
            out.write(RedisRDBContent.RDBFile);
            out.flush();
            store.replicaOutputStreams.add(out);
        }
    }
    
}
