package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisData;
import store.RedisStore;

public class GetCommand implements Command {
    @Override
    public void execute(List<String> input , OutputStream out , RedisStore store) throws Exception {
        RedisData res = store.data.get(input.get(1));
        long curTime = System.currentTimeMillis();
        if(curTime > res.expiryTime){
            out.write("$-1\r\n".getBytes());
            store.data.remove(input.get(1));
        }
        else {
            out.write(("$" + res.value.length() + "\r\n" + res.value + "\r\n").getBytes());
            out.flush();
        }
    }
}
