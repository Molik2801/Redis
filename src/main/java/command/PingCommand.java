package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class PingCommand implements Command {
    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String response = "+PONG\r\n";
        out.write(response.getBytes());
        out.flush();
    }
}
