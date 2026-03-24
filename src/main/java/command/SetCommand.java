package command;

import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import store.RedisData;
import store.RedisStore;

public class SetCommand implements Command {

    @Override
    public void execute(List<String> input , OutputStream out, RedisStore store) throws Exception {
        RedisData data = new RedisData();
        data.value = input.get(2);
        if(input.size() >= 5){
            if(Objects.equals(input.get(3), "EX")){
                data.expiryTime = System.currentTimeMillis() + Integer.parseInt(input.get(4))* 100L;
            }
            else if(Objects.equals(input.get(3), "PX")){
                data.expiryTime = System.currentTimeMillis() + Integer.parseInt(input.get(4));
            }
        }
        store.data.put(input.get(1) , data);
        out.write("+OK\r\n".getBytes());
        out.flush();

        store.broadcastToReplicas(input);
    }
    
}
