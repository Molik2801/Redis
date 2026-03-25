package command;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import store.RedisStore;

public class PublishCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {

        String channelName = input.get(1);
        String message = input.get(2);

        int subscribedClients = 0;

        for(Map.Entry<OutputStream , Set<String>> entry : store.subscriptions.entrySet()){
            OutputStream clientStream = entry.getKey();
            Set<String> myChannels = entry.getValue();

            if(myChannels.contains(channelName)){
                clientStream.write(("*3\r\n$7\r\nmessage\r\n$" + channelName.length() + "\r\n" + channelName + "\r\n$" + message.length() + "\r\n" + message + "\r\n").getBytes());
                clientStream.flush();
                subscribedClients++;
            }
        }
        out.write((":" + subscribedClients + "\r\n").getBytes());
        out.flush();
    }
    
}
