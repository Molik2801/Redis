package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class ReplConfCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {

        if(input.size() > 1 && input.get(1).equals("GETACK")){
            out.write("*3\r\n$8\r\nREPLCONF\r\n$3\r\nACK\r\n$1\r\n0\r\n".getBytes());
            out.flush();
        }
        else{
            out.write("+OK\r\n".getBytes());
            out.flush();
        }
    }
    
}
