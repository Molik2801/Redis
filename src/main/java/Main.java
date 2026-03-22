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

        int portNumber = 6379; // Default Redis port

        //Custom port support
        if(args.length > 0 && args[0].equals("--port")){
            try {
                portNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default port 6379.");
            }
        }
        System.out.println("Starting Redis Clone on port " + portNumber + "...");
        
        final int port = portNumber;
        // Master-Slave support
        if(args.length > 2){
            if(args[2].equals("--replicaof")){
                store.isSlave = true;
                store.masterHost = args[3].split(" ")[0];
                store.masterPort = Integer.parseInt(args[3].split(" ")[1]);     
            }
        }

        //Master-Slave Replication Thread
        if(store.isSlave){
            new Thread(() -> {

                try(Socket masterSocket = new Socket(store.masterHost , store.masterPort)){

                    OutputStream masterOutput = masterSocket.getOutputStream();
                    InputStream masterInput = masterSocket.getInputStream();

                    // First PING Command
                    masterOutput.write("*1\r\n$4\r\nPING\r\n".getBytes());
                    masterOutput.flush();

                    byte[] buffer = new byte[1024];
                    int byteCount = masterInput.read(buffer);

                    masterOutput.write(("*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$4\r\n" + port + "\r\n").getBytes());
                    masterOutput.flush();

                    byteCount = masterInput.read(buffer);

                    masterOutput.write("*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n".getBytes());
                    masterOutput.flush();

                    byteCount = masterInput.read(buffer);
                    
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
                ClientHandler clientHandler = new ClientHandler(clientSocket, store, registry);
                Thread worker = new Thread(clientHandler);
                worker.start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}