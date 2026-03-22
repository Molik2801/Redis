package server;

import command.Command;
import command.CommandRegistry;
import protocol.Parser;
import store.RedisStore;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final RedisStore store;
    private final CommandRegistry registry;
    private final boolean isReplica;
    // Constructor Injection
    public ClientHandler(Socket clientSocket, RedisStore store, CommandRegistry registry , boolean isReplica) {
        this.clientSocket = clientSocket;
        this.store = store;
        this.registry = registry;
        this.isReplica = isReplica;
    }

    @Override
    public void run() {
        try {
            
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream;

            if(isReplica){
                outputStream = new OutputStream() {
                    @Override
                    public void write(int b){
                        //No replies to replicas
                    }
                };
            }
            else{
                outputStream = clientSocket.getOutputStream();
            }

            Parser parser = new Parser();
            
            //MULTI And EXEC 
            boolean inQueue = false;
            List<List<String>> queuedCommands = new ArrayList<>();

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

                // 2. Queuing Commands
                if(commandName.equals("MULTI")){
                    inQueue = true;
                    outputStream.write("+OK\r\n".getBytes());
                    outputStream.flush();
                    continue;
                }
                if(commandName.equals("EXEC")){
                    if(!inQueue){
                        outputStream.write("-ERR EXEC without MULTI\r\n".getBytes());
                        outputStream.flush();
                        continue;
                    }
                    inQueue = false;
                    outputStream.write(("*" + queuedCommands.size() + "\r\n").getBytes());
                    for(List<String> cmd : queuedCommands){
                        Command c = registry.getCommand(cmd.get(0));
                        c.execute(cmd , outputStream , store);
                    }
                    queuedCommands.clear();
                    continue;
                }
                if(commandName.equals("DISCARD")){
                    if(!inQueue){
                        outputStream.write("-ERR DISCARD without MULTI\r\n".getBytes());
                        outputStream.flush();
                        continue;
                    }
                    inQueue = false;
                    queuedCommands.clear();
                    outputStream.write("+OK\r\n".getBytes());
                    outputStream.flush();
                    continue;
                }
                if(inQueue){
                    queuedCommands.add(inputList);
                    outputStream.write("+QUEUED\r\n".getBytes());
                    outputStream.flush();
                    continue;
                }

                // Other Commands 
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