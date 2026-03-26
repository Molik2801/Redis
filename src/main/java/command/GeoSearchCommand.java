package command;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import store.RedisStore;
import store.RedisZSet;

public class GeoSearchCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        String method = input.get(2);
        double centreLongitude = Double.parseDouble(input.get(3));
        double centreLatitude = Double.parseDouble(input.get(4));
        String method2 = input.get(5);
        double radius = Double.parseDouble(input.get(6));
        String unit = input.get(7);

        RedisZSet zSet = store.sortedSet.get(key);
        if(zSet == null){
            out.write("*-1\r\n".getBytes());
            out.flush();
            return;
        }

        List<String> locations = new ArrayList<>();

        for(Map.Entry<String , Double> entry : zSet.memberScores.entrySet()){
            String location = entry.getKey();
            double score = entry.getValue();

            double latitude = zSet.scoreToLocation(score).get(0);
            double longitude = zSet.scoreToLocation(score).get(1);

            double distance = zSet.dist(longitude, latitude, centreLongitude, centreLatitude);

            if(distance <= radius)locations.add(location);
        }

        StringBuilder resp = new StringBuilder();
        resp.append("*" + locations.size() + "\r\n");
        for(String entry : locations){
            resp.append("$" + entry.length() + "\r\n" + entry + "\r\n");
        }

        out.write(resp.toString().getBytes());
        out.flush();
    }
    
}
