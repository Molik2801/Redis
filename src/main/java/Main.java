import command.CommandRegistry;
import server.ClientHandler;
import store.RedisStore;

import java.io.IOException;
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
        if(store.isSlave){
            try (Socket masterSocket = new Socket(store.masterHost , store.masterPort)){
                OutputStream masterOut = masterSocket.getOutputStream();
                masterOut.write("*1\r\n$4\r\nPING\r\n".getBytes());
                masterOut.flush();
            } catch (IOException e) {
                System.out.println("Failed to connect to master: " + e.getMessage());
            }
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