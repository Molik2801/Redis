import java.io.*;
import java.util.List;

public class CommandHandler {

    static void commandResponse(List<String> input , OutputStream outputStream) throws IOException {
        if(input.get(0).equals("PING")){
            outputStream.write("+PONG\r\n".getBytes());
        }
        else if(input.get(0).equalsIgnoreCase("ECHO")){
            outputStream.write(input.get(1).getBytes());
        }
    }
}
