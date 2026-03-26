package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;
import store.RedisZSet;

public class ZCardCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {

       String key = input.get(1);

       RedisZSet zSet = store.sortedSet.get(key);

       int cardinality = 0;
       if(zSet != null) cardinality = zSet.memberScores.size();

       out.write((":" + cardinality + "\r\n").getBytes());
       out.flush();
    }
    
}
