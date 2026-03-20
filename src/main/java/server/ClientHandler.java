package server;

import command.Command;
import command.CommandRegistry;
import protocol.Parser;
import store.RedisStore;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final RedisStore store;
    private final CommandRegistry registry;

    // Constructor Injection
    public ClientHandler(Socket clientSocket, RedisStore store, CommandRegistry registry) {
        this.clientSocket = clientSocket;
        this.store = store;
        this.registry = registry;
    }

    @Override
    public void run() {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {
            
            Parser parser = new Parser();
            
            while (true) {
                byte[] input = new byte[1024];
                int byteCount = inputStream.read(input);
                if (byteCount == -1) break; // Client disconnected

                String inputString = new String(input, 0, byteCount).trim();
                List<String> inputList = parser.parse(inputString);

                if (inputList == null || inputList.isEmpty()) continue;

                // 1. Look up the command in the registry
                String commandName = inputList.get(0);
                Command command = registry.getCommand(commandName);

                // 2. Execute it, passing the store!
                if (command != null) {
                    command.execute(inputList, outputStream, store);
                } else {
                    outputStream.write(("-ERR unknown command '" + commandName + "'\r\n").getBytes());
                }
            }
        } catch (Exception e) {
            System.out.println("Client Error: " + e.getMessage());
        }
    }
}