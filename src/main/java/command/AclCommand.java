package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class AclCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String command = input.get(1);
        StringBuilder resp = new StringBuilder();

        if(command.toUpperCase().equals("WHOAMI")){
            resp.append("$7\r\ndefault\r\n");
        }
        if(command.toUpperCase().equals("GETUSER")){
            String user = input.get(2);
            resp.append("*2\r\n");
            resp.append("$5\r\nflags\r\n");
            resp.append("*1\r\n");
            resp.append("$6\r\nnopass\r\n");
        }

        out.write(resp.toString().getBytes());
        out.flush();
    }
    
}
