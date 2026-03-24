package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class ConfigCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        if(input.get(1).equals("GET")){
            StringBuilder resp = new StringBuilder();
            resp.append("*2\r\n");
            resp.append("$" + input.get(2).length() + "\r\n" + input.get(2) + "\r\n");
            String data = "";
            if(input.get(2).toLowerCase().equals("dir"))data = store.dirName;
            if(input.get(2).toLowerCase().equals("dbfilename")) data = store.dbFileName;

            resp.append("$" + data.length() + "\r\n" + data + "\r\n");
            out.write(resp.toString().getBytes());
            out.flush();
        }
    }
    
}
