package command;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import store.RedisStore;

public class SubscribeCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {

        Set<String> myChannels = store.subscriptions.computeIfAbsent(out , key -> new HashSet<>());

        for(int i = 1 ; i < input.size() ; i++){
            
            String channel = input.get(i);
            myChannels.add(channel);
            int channelsCount = myChannels.size();
    
            StringBuilder resp = new StringBuilder();
            resp.append("*3\r\n$9\r\nsubscribe\r\n" + "$" + channel.length() + "\r\n" + channel + "\r\n:" + channelsCount + "\r\n");
            out.write(resp.toString().getBytes());
            out.flush();
        }
    }
    
}
