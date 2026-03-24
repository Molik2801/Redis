package server;

import command.Command;
import command.CommandRegistry;
import protocol.Parser;
import store.RedisStore;
import store.ReplicaConnection;

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
    private ReplicaConnection myConnection = null;

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

            OutputStream realStream = clientSocket.getOutputStream();
            OutputStream blankOutputStream = new OutputStream() {
                @Override
                public void write(int b){
                     //No replies fro replicas
                }
            };

            Parser parser = new Parser();

            //MULTI AND EXEC
            boolean inQueue = false;
            List<List<String>> queuedCommands = new ArrayList<>();

            while (true) {
                
                byte[] input = new byte[1024];
                int byteCount = inputStream.read(input);
                if (byteCount == -1) break; // Client disconnected

                String inputString = new String(input, 0, byteCount).trim();
                List<List<String>> commandList = parser.parse(inputString);

                if(isReplica) store.ackOffset += byteCount;

                if (commandList == null || commandList.isEmpty()) continue;

                for(List<String> inputList : commandList){

                    String commandName = inputList.get(0);
                    Command command = registry.getCommand(commandName);
                    
                    //Replica-Master offset maintenance
                    //Replica sending PSYNC command to master 
                    //now the particular master-replica connection is maintained by this thread and the sticky variable myconnection is stored in master's component on the particular thread for that replica
                    //and when multiple replica come there will be multiple connection , different thread , different myConnection variable, all in master's side
                    if(commandName.equals("PSYNC")){
                        
                        this.myConnection = new ReplicaConnection(realStream);
                        store.replicaOutputStreams.add(this.myConnection);
                        command.execute(inputList, realStream, store);
                        continue;

                    }

                    //Master getting reply of GetAck command i.e offset of that particular replica and updating the offset of the particular replica connected to this thread in its myconnection variable.
                    if(commandName.equals("REPLCONF") && inputList.size() > 2 && inputList.get(1).equals("ACK")){
                        int ackOffset = Integer.parseInt(inputList.get(2));
                        if(this.myConnection != null) this.myConnection.ackOffset = ackOffset;
                        continue;
                    }                

                    // Assigning outputstream
                    if(isReplica){
                        if(commandName.equals("REPLCONF")){ 
                            outputStream = realStream;
                            store.ackOffset -= 37;
                        }
                        else outputStream = blankOutputStream;
                    }
                    else outputStream = realStream;

                    // Queuing Commands
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
            }
        } catch (Exception e) {
            System.out.println("Client Error: " + e.getMessage());
        }
    }
}

