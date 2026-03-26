package command;

import store.RedisStore;
import java.io.OutputStream;
import java.util.List;

public interface Command {
    void execute(List<String> input, OutputStream out, RedisStore store) throws Exception;
}