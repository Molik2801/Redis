package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class InfoCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        StringBuilder info = new StringBuilder();
        if(input.get(1).equals("replication")){
            String role = "role:master";
            if(store.isSlave) role = "role:slave";
            info.append("$" + role.length() + "\r\n" + role + "\r\n");
        }
        out.write(info.toString().getBytes());
        out.flush();
    }
    
}
