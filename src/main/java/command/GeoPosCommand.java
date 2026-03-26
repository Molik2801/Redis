package command;

import java.io.OutputStream;
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
            double latitude = zSet.scoreToLocation(score).get(0);
            double longitude = zSet.scoreToLocation(score).get(1);
    
            String latString = String.valueOf(latitude);
            String lonString = String.valueOf(longitude);
    
            resp.append("*2\r\n");
            resp.append("$" + lonString.length() + "\r\n" + lonString + "\r\n");
            resp.append("$" + latString.length() + "\r\n" + latString + "\r\n");
        }

        out.write(resp.toString().getBytes());
        out.flush();
    }
    
}
