package command;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {
        // Register all your commands here
        commands.put("BLPOP", new BlPopCommand());
        commands.put("RPUSH", new RPushCommand());
        commands.put("PING", new PingCommand());
        commands.put("SET", new SetCommand());
        commands.put("GET", new GetCommand());
        commands.put("ECHO", new EchoCommand());
        commands.put("LPUSH", new LPushCommand());  
        commands.put("LRANGE", new LRangeCommand());
        commands.put("LPOP", new LPopCommand());
        commands.put("LLEN", new LLenCommand());
        commands.put("TYPE", new TypeCommand());
        commands.put("XADD", new XaddCommand());
        commands.put("XRANGE" , new XRangeCommand());
        commands.put("XREAD" , new XReadCommand());
        commands.put("INCR" , new INCRCommand());
        commands.put("INFO" , new InfoCommand());
        commands.put("REPLCONF" , new ReplConfCommand());
        commands.put("PSYNC" , new PSyncCommand());
        commands.put("WAIT" , new WaitCommand());
        commands.put("CONFIG", new ConfigCommand());
        commands.put("SUBSCRIBE", new SubscribeCommand());
        commands.put("PUBLISH", new PublishCommand());
        commands.put("UNSUBSCRIBE", new UnsubscribeCommand());
        commands.put("ZADD", new ZAddCommand());
        commands.put("ZRANK", new ZRankCommand());
        commands.put("ZRANGE", new ZRangeCommand());
        commands.put("ZCARD", new ZCardCommand());
        commands.put("ZSCORE", new ZScoreCommand());
        commands.put("ZREM", new ZRemCommand());
    }

    public Command getCommand(String name) {
        return commands.get(name.toUpperCase());
    }
}