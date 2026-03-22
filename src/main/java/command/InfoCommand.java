package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class InfoCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        StringBuilder info = new StringBuilder();
        if(input.get(1).equals("replication")){
            String role = "master";
            if(store.isSlave) role = "slave";

            String masterReplId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
            String masterOffset = "0";

            String finalInfo = "role:" + role + "\r\n" + "master_replid:" + masterReplId + "\r\n" + "master_repl_offset:" + masterOffset;
            info.append("$" + finalInfo.length() + "\r\n" + finalInfo + "\r\n");
        }
        out.write(info.toString().getBytes());
        out.flush();
    }
    
}
