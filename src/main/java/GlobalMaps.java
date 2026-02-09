import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalMaps {
    public static final ConcurrentHashMap<String , RedisData> data = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String , ArrayList> list = new ConcurrentHashMap<>();
}
