import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class CommandHandler {

    void commandResponse(Socket clientSocket) throws IOException {
        clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
    }
}
