import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler {

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
            GlobalMaps.data.put(input.get(1) , data);
            outputStream.write("+OK\r\n".getBytes());
        }
        else if(input.get(0).equals("GET")){
            RedisData res = GlobalMaps.data.get(input.get(1));
            long curTime = System.currentTimeMillis();
            if(curTime > res.expiryTime){
                outputStream.write("$-1\r\n".getBytes());
                GlobalMaps.data.remove(input.get(1));
            }
            else {
                outputStream.write(("$" + res.value.length() + "\r\n" + res.value + "\r\n").getBytes());
            }
        }
        else if(input.get(0).equals("RPUSH")){
            int size = 1;
            if(GlobalMaps.list.containsKey(input.get(1))){
                System.out.println(size + " Exist");
                GlobalMaps.list.get(input.get(1)).add(input.get(2));
                size = GlobalMaps.list.get(input.get(1)).size();
            }
            else {
                ArrayList curList = new ArrayList();
                curList.add(input.get(2));
                System.out.println(size + " Empty");
                GlobalMaps.list.put(input.get(1) , curList);
            }
            outputStream.write((":" + size + "\r\n").getBytes());
        }
    }
}
