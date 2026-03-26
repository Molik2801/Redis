package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;
import store.RedisZSet;
import store.ZSetEntry;

public class ZAddCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        
        String key = input.get(1);
        Double score = Double.parseDouble(input.get(2));
        String member = input.get(3);

        ZSetEntry entry = new ZSetEntry(score , member);

        store.sortedSet.computeIfAbsent(key, k -> new RedisZSet());
        
        RedisZSet zSet = store.sortedSet.get(key);

        int memberAdded = 1;
        if(zSet.memberScores.containsKey(member)){
            Double oldScore = zSet.memberScores.get(member);
            ZSetEntry oldEntry = new ZSetEntry(oldScore, member);
            zSet.orderedSet.remove(oldEntry);
            memberAdded = 0;
        }
        zSet.memberScores.put(member, score);
        zSet.orderedSet.add(entry);

        out.write((":" + memberAdded + "\r\n").getBytes());
        out.flush();
    }
}
