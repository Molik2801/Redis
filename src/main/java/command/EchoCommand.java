package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class EchoCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String res = input.get(1);
        out.write(("$" + res.length() + "\r\n" + res + "\r\n").getBytes());
        out.flush();
    }
    
}
