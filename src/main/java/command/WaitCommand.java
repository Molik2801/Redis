package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;
import store.ReplicaConnection;

public class WaitCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
       int replicaCount = 0;
       long timeOut = Integer.parseInt(input.get(2));
       int minReplicas = Integer.parseInt(input.get(1));
       
       //If masterOffset is 0 means data is not shared yet and all replicas are up to date
       if(store.masterOffset == 0){
            out.write((":" + store.replicaOutputStreams.size() + "\r\n").getBytes());
            out.flush();
            return;
       }

       // now if masteroffset is greater than 0 then we have to check whats the state of each of the replicas which can be found by getting their offsets using getack command.
       for(ReplicaConnection replicas : store.replicaOutputStreams){
           replicas.outputStream.write("*3\r\n$8\r\nREPLCONF\r\n$6\r\nGETACK\r\n$1\r\n*\r\n".getBytes());
           replicas.outputStream.flush();
       }

       //now we will wait upto timeout time to check if we get the updated offset or the replicas have replied to out getack command updating their last write offset 
       //then if their own offset are equal to master write offset means they are upto date
       long curTime = System.currentTimeMillis();
       while(System.currentTimeMillis() - timeOut < curTime){
            int curReplicaCount = 0;
            for(ReplicaConnection replicas : store.replicaOutputStreams){
                if(replicas.ackOffset >= store.masterOffset)curReplicaCount++;
            }
            replicaCount = curReplicaCount;
            if(replicaCount >= minReplicas)break;
            Thread.sleep(10);
       }
       out.write((":" + replicaCount + "\r\n").getBytes());
       out.flush();
    }
    
}
