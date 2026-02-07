import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler {
    static ConcurrentHashMap<String , String> storage = new ConcurrentHashMap<>();

    static void commandResponse(List<String> input , OutputStream outputStream) throws IOException {
        if(input == null || input.isEmpty()){
            return; 
        }
        if(input.get(0).equals("PING")){
            outputStream.write("+PONG\r\n".getBytes());
        }
        else if(input.get(0).equalsIgnoreCase("ECHO")){
            String res = input.get(1);
            outputStream.write(("$" + res.length() + "\r\n" + res + "\r\n").getBytes());
        }
        else if(input.get(0).equals("SET")){
            storage.put(input.get(1) , input.get(2));
            outputStream.write("+OK\r\n".getBytes());
        }
        else if(input.get(0).equals("GET")){
            String res = storage.get(input.get(1));
            outputStream.write(("$" + res.length() + "\r\n" + res + "\r\n").getBytes());
        }
    }
}
