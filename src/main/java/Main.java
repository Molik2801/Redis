import command.CommandRegistry;
import server.ClientHandler;
import store.RedisStore;

import java.io.IOException;
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