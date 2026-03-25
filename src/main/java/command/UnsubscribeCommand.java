package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class UnsubscribeCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
       String channelName = input.get(1);
       
       if(store.subscriptions.get(out).contains(channelName)){
           store.subscriptions.get(out).remove(channelName);
       }
       
       int channelsCount = store.subscriptions.get(out).size();

       StringBuilder resp = new StringBuilder();
       resp.append("*3\r\n$11\r\nunsubscribe\r\n" + "$" + channelName.length() + "\r\n" + channelName + "\r\n:" + channelsCount + "\r\n");
       out.write(resp.toString().getBytes());
       out.flush();
    }
    
}
