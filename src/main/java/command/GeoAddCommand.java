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
       double longitude = Double.parseDouble(input.get(2));
       double latitude = Double.parseDouble(input.get(3));
       String place = input.get(4);

       double lonLimit = (double) 180;
       double latLimit = 85.05112878;

       if(longitude > lonLimit || longitude < -lonLimit || latitude > latLimit || latitude < -latLimit){
            out.write(("-ERR invalid longitude,latitude pair" + longitude + "," + latitude + "\r\n").getBytes());
            out.flush();
            return;
       }

       double score = locationToScore(latitude , longitude);
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

    public double locationToScore(double latitude , double longitude){
        
        double latMax = 85.05112878;
        double lonMax = 180;

        double normalised_latitude = ((1 << 26) * (latitude + latMax)) / (latMax * 2);
        double normalised_longitude = ((1 << 26) * (longitude + lonMax)) / (lonMax * 2);

        long score = interleave((int) normalised_latitude , (int) normalised_longitude);
        return (double) score;
    }

    public long interleave(int x , int y){
        long z = 0;
        for(int i = 0 ; i < 32 ; i++){
            z |= (long) (x & (1 << i)) << i;
            z |= (long) (y & (1 << i)) << (i+1);
        }
        return z;
    }
    
}
