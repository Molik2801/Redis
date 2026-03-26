package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;
import store.RedisZSet;
import store.ZSetEntry;

public class GeoAddCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
       String key = input.get(1);
       Double longitude = Double.parseDouble(input.get(2));
       Double latitude = Double.parseDouble(input.get(3));
       String place = input.get(4);

       Double lonLimit = (double) 180;
       Double latLimit = 85.05112878;

       if(longitude > lonLimit || longitude < -lonLimit || latitude > latLimit || latitude < -latLimit){
            out.write(("-ERR invalid longitude,latitude pair" + longitude + "," + latitude + "\r\n").getBytes());
            out.flush();
            return;
       }

       double score = 0;
       String sscore = String.valueOf(score);

       store.sortedSet.computeIfAbsent(key, k -> new RedisZSet());
       RedisZSet zSet = store.sortedSet.get(key);
       ZSetEntry entry = new ZSetEntry(score, place);

       int memberAdded = 1;
       if(zSet.memberScores.containsKey(place)){
           Double oldScore = zSet.memberScores.get(place);
           ZSetEntry oldEntry = new ZSetEntry(oldScore, place);
           zSet.orderedSet.remove(oldEntry);
           memberAdded = 0;
       }
       zSet.memberScores.put(place, score);
       zSet.orderedSet.add(entry);

       out.write((":" + memberAdded + "\r\n").getBytes());
       out.flush();
    }
    
}
