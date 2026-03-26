package command;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import store.RedisStore;
import store.RedisZSet;

public class GeoPosCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        int placeNum = input.size() - 2;

        StringBuilder resp = new StringBuilder();
        resp.append("*" + placeNum + "\r\n");

        RedisZSet zSet = store.sortedSet.get(key);

        for(int i = 2 ; i < input.size() ; i++){

            String place = input.get(i);
            if(zSet == null || (zSet != null && !zSet.memberScores.containsKey(place))){
                resp.append("*-1\r\n");
                continue;
            }
    
            double score = zSet.memberScores.get(place);
            double latitude = scoreToLocation(score).get(0);
            double longitude = scoreToLocation(score).get(1);
    
            String latString = String.valueOf(latitude);
            String lonString = String.valueOf(longitude);
    
            resp.append("*2\r\n");
            resp.append("$" + lonString.length() + "\r\n" + lonString + "\r\n");
            resp.append("$" + latString.length() + "\r\n" + latString + "\r\n");
        }

        out.write(resp.toString().getBytes());
        out.flush();
    }
    
    public List<Double> scoreToLocation(double score){
        long z = (long) score;
        int x = 0;
        int y = 0;
        for(int i = 0 ; i < 64 ; i++){
            if(i % 2 == 0) x |= ((z >> i) & 1L) << (i/2);
            else y |= ((z >> i) & 1L) << (i/2);
        }

        double minLatitude = -85.05112878;
        double minLongitude = -180;
        double latitudeRange = Math.abs(minLatitude) * 2;
        double longitudeRange = Math.abs(minLongitude) * 2;

        double gridLatmin = minLatitude + latitudeRange * ((double) x / (1 << 26));
        double gridLatmax = minLatitude + latitudeRange * ((double) (x + 1) / (1 << 26));
        double gridLonmin = minLongitude + longitudeRange * ((double) y / (1 << 26));
        double gridLonmax = minLongitude + longitudeRange * ((double) (y+1) / (1 << 26));

        double latitude = (gridLatmin + gridLatmax) / 2;
        double longitude = (gridLonmin + gridLonmax) / 2;

        return Arrays.asList(latitude , longitude);
    }
}
