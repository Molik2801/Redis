package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;
import store.RedisZSet;

public class ZScoreCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        String member = input.get(2);

        RedisZSet zSet = store.sortedSet.get(key);

        if(zSet != null && zSet.memberScores.containsKey(member)){
            String score = String.valueOf(zSet.memberScores.get(member));
            out.write(("$" + score.length() + "\r\n" + score + "\r\n").getBytes());
            out.flush();
        }
        else{
            out.write("$-1\r\n".getBytes());
            out.flush();
        }
    }
    
}
