import java.io.*;
import java.lang.reflect.Array;
import java.sql.SQLOutput;
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
            if(GlobalMaps.list.containsKey(input.get(1))){
                for(int i = 2 ; i < input.size() ; i++){
//                    System.out.println(i + " " + input.get(i) + " hi");
                    GlobalMaps.list.get(input.get(1)).add(input.get(i));
                }
            }
            else {
                ArrayList curList = new ArrayList();
                for(int i = 2 ; i < input.size() ; i++){
//                    System.out.println(i + " " + input.get(i) + " hi1");
                    curList.add(input.get(i));
                }
                GlobalMaps.list.put(input.get(1) , curList);
            }
            int size = GlobalMaps.list.get(input.get(1)).size();
            outputStream.write((":" + size + "\r\n").getBytes());
        }
        else if(input.get(0).equals("LRANGE")){
            int l = Integer.parseInt(input.get(2));
            int r = Integer.parseInt(input.get(3));
            String listName = input.get(1);
            StringBuilder respBulk = new StringBuilder();

            if(GlobalMaps.list.containsKey(listName)){
                if(l < 0){
                    l = Math.max(0 , GlobalMaps.list.get(listName).size() + l);
                }
                if(r < 0){
                    r = Math.max(0 , GlobalMaps.list.get(listName).size() + r);
                }
                int size = Math.min(r , GlobalMaps.list.get(listName).size() - 1) - Math.max(0 , l) + 1;
                respBulk.append("*" + size + "\r\n");
                System.out.println(size);
                for(int i = Math.max(0 , l) ; i <= Math.min(r , GlobalMaps.list.get(listName).size() - 1) ; i++){
                    String element = GlobalMaps.list.get(listName).get(i).toString();
                    System.out.println(i + " " + element);
                    respBulk.append("$").append(element.length()).append("\r\n").append(element).append("\r\n");
                }
            }
            else{
                respBulk.append("*0\r\n");
            }
            outputStream.write(respBulk.toString().getBytes());

        }
    }
}
