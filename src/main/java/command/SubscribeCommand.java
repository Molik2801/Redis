package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class SubscribeCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String channel = input.get(1);
        StringBuilder resp = new StringBuilder();
        resp.append("*3\r\n$9\r\nsubscribe\r\n" + "$" + channel.length() + "\r\n" + channel + "\r\n:1\r\n");
        out.write(resp.toString().getBytes());
        out.flush();
    }
    
}
