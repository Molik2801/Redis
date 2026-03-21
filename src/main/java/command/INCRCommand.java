package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class INCRCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        store.RedisData redisData = store.data.get(key);

        int numValue;
        if(redisData == null) {
            // System.out.println("hi");
            numValue = 1;
        } else {
            String entry = redisData.value;
            try {
                numValue = Integer.parseInt(entry) + 1;
            } 
            catch (NumberFormatException e) {
                out.write("-ERR value is not an integer or out of range\r\n".getBytes());
                out.flush();
                return;
            }
        }

        store.data.putIfAbsent(key , new store.RedisData());
        store.data.get(key).value = String.valueOf(numValue);
        out.write((":" + numValue + "\r\n").getBytes());
        out.flush();
    }
    
}
