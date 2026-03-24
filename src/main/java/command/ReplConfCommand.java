package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class ReplConfCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {

        if(input.size() > 1 && input.get(1).equals("GETACK")){
            String ack = String.valueOf(store.ackOffset);
            out.write(("*3\r\n$8\r\nREPLCONF\r\n$3\r\nACK\r\n$" + ack.length() + "\r\n" + ack + "\r\n").getBytes());
            out.flush();
            store.ackOffset += 37;
        }
        else{
            out.write("+OK\r\n".getBytes());
            out.flush();
        }
    }
    
}
