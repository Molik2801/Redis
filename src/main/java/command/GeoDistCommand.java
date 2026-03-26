package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;
import store.RedisZSet;

public class GeoDistCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        String placeA = input.get(2);
        String placeB = input.get(3);

        RedisZSet zSet = store.sortedSet.get(key);

        if(zSet == null || (zSet != null && (!zSet.memberScores.containsKey(placeA) || !zSet.memberScores.containsKey(placeB)))){
            out.write("*-1\r\n".getBytes());
            out.flush();
            return;
        }

        double scoreA = zSet.memberScores.get(placeA);
        double scoreB = zSet.memberScores.get(placeB);

        double latA = zSet.scoreToLocation(scoreA).get(0);
        double lonA = zSet.scoreToLocation(scoreA).get(1);
        double latB = zSet.scoreToLocation(scoreB).get(0);
        double lonB = zSet.scoreToLocation(scoreB).get(1);

        double distance = zSet.dist(lonA, latA, lonB, latB);
        String dis = String.valueOf(distance);

        out.write(("$" + dis.length() + "\r\n" + dis + "\r\n").getBytes());
        out.flush();
    }
    
}
