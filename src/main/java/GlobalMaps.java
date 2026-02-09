import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalMaps {
    public static final ConcurrentHashMap<String , RedisData> data = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String , ArrayDeque<String>> list = new ConcurrentHashMap<>();
}
