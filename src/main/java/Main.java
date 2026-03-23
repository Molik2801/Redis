import command.CommandRegistry;
import server.ClientHandler;
import store.RedisStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args){
        
        // Instantiate your centralized architecture
        RedisStore store = new RedisStore();
        CommandRegistry registry = new CommandRegistry();

        int port = 6379; // Default Redis port

        //Custom port support
        if(args.length > 0 && args[0].equals("--port")){
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default port 6379.");
            }
        }
        System.out.println("Starting Redis Clone on port " + port + "...");

        // Master-Slave support
        if(args.length > 2){
            if(args[2].equals("--replicaof")){
                store.isSlave = true;
                store.masterHost = args[3].split(" ")[0];
                store.masterPort = Integer.parseInt(args[3].split(" ")[1]);     
            }
        }

        //Master-Slave Replication Thread
        final int finalPort = port;
        if(store.isSlave){
            new Thread(() -> {

                try(Socket replicaSocket = new Socket(store.masterHost , store.masterPort)){

                    OutputStream replicaOutput = replicaSocket.getOutputStream();
                    InputStream replicaInput = replicaSocket.getInputStream();

                    byte[] buffer = new byte[1024];
                    
                    //Handshakes

                    replicaOutput.write("*1\r\n$4\r\nPING\r\n".getBytes());
                    replicaOutput.flush();
                    int byteCount = replicaInput.read(buffer);

                    replicaOutput.write(("*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$4\r\n" + finalPort + "\r\n").getBytes());
                    replicaOutput.flush();

                    byteCount = replicaInput.read(buffer);

                    replicaOutput.write("*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n".getBytes());
                    replicaOutput.flush();

                    byteCount = replicaInput.read(buffer);

                    replicaOutput.write("*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n".getBytes());
                    replicaOutput.flush();

                    // 1. Read the +FULLRESYNC line
                    String resyncResponse = readLine(replicaInput);

                    // 2. Read the RDB length header (e.g., "$88\r\n")
                    String rdbHeader = readLine(replicaInput); 

                    // 3. Extract the integer 88 from the header
                    int rdbLength = Integer.parseInt(rdbHeader.substring(1, rdbHeader.length() - 2));

                    // 4. Read EXACTLY 88 bytes from the pipe to swallow the file
                    byte[] rdbFile = new byte[rdbLength];
                    int bytesRead = 0;
                    while (bytesRead < rdbLength) {
                        bytesRead += replicaInput.read(rdbFile, bytesRead, rdbLength - bytesRead);
                    }

                    //Now synced and ready for further commands
                    ClientHandler masterListener = new ClientHandler(replicaSocket , store , registry , true);
                    masterListener.run();

                } catch (IOException e) {
                    System.out.println("Failed to connect to master: " + e.getMessage());
                }
            }).start();
        }

        //ClientSocket handling
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            
            while(true){
                Socket clientSocket = serverSocket.accept();
                // Pass the architecture into the handler
                ClientHandler clientHandler = new ClientHandler(clientSocket, store, registry , false);
                Thread worker = new Thread(clientHandler);
            
                worker.start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }


    private static String readLine(InputStream in) throws IOException {
    StringBuilder sb = new StringBuilder();
    int b;
    while ((b = in.read()) != -1) {
        sb.append((char) b);
        if (sb.toString().endsWith("\r\n")) {
            break;
        }
    }
    return sb.toString();
}
}