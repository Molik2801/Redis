package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;
import store.RedisZSet;
import store.ZSetEntry;

public class ZRankCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        String member = input.get(2);

        RedisZSet zSet = store.sortedSet.get(key);
        if(zSet == null || !zSet.memberScores.containsKey(member)){
            out.write("$-1\r\n".getBytes());
            out.flush();
            return;
        }

        int rank = 0;
        for(ZSetEntry entry : zSet.orderedSet){
            if(entry.member.equals(member))break;
            rank++;
        }

        out.write((":" + rank + "\r\n").getBytes());
        out.flush();
    }
    
}
