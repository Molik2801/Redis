package store;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class RedisZSet{
    
    public ConcurrentHashMap<String , Double> memberScores = new ConcurrentHashMap<>();
    public ConcurrentSkipListSet<ZSetEntry> orderedSet = new ConcurrentSkipListSet<>();

}
