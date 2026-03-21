package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class INCRCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        String entry = store.data.get(key).value;

        int numValue;
        if(entry == null) {
            numValue = 1;
        } else {
            numValue = Integer.parseInt(entry) + 1;
        }
        store.data.get(key).value = String.valueOf(numValue);
        out.write((":" + numValue + "\r\n").getBytes());
        out.flush();
    }
    
}
