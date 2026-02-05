import java.io.*;
import java.net.Socket;

public class CommandHandler {

    void commandResponse(Socket clientSocket){
        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            while(true){
                byte[] input = new byte[1024];
                int byteCount = inputStream.read(input);
                String inputString = new String(input).trim();
                outputStream.write("+PONG\r\n".getBytes());
            }
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
