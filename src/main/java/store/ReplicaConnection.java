package store;

import java.io.OutputStream;

public class ReplicaConnection {
    public OutputStream outputStream;
    public int ackOffset;

    public ReplicaConnection(OutputStream outputStream){
        this.outputStream = outputStream;
        this.ackOffset = 0;
    }
}
