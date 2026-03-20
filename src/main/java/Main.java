import command.CommandRegistry;
import server.ClientHandler;
import store.RedisStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args){
        System.out.println("Starting Redis Clone on port 6379...");

        // Instantiate your centralized architecture
        RedisStore store = new RedisStore();
        CommandRegistry registry = new CommandRegistry();

        int port = 6379;
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