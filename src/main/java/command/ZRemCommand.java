package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;
import store.RedisZSet;
import store.ZSetEntry;

public class ZRemCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        String member = input.get(2);

        RedisZSet zSet = store.sortedSet.get(key);
        int removed = 0;

        if(zSet != null && zSet.memberScores.containsKey(member)){
            ZSetEntry entry = new ZSetEntry(zSet.memberScores.get(member), member);
            zSet.memberScores.remove(member);
            zSet.orderedSet.remove(entry);
            removed = 1;
        }

        out.write((":" + removed + "\r\n").getBytes());
        out.flush();
    }
    
}
