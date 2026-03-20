package command;

import store.RedisStore;
import java.io.OutputStream;
import java.util.List;

public interface Command {
    void execute(List<String> args, OutputStream out, RedisStore store) throws Exception;
}