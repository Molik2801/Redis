package store;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class RedisZSet{
    public final double EarthsRadius = 6372797.560856;
    public ConcurrentHashMap<String , Double> memberScores = new ConcurrentHashMap<>();
    public ConcurrentSkipListSet<ZSetEntry> orderedSet = new ConcurrentSkipListSet<>();

    //for geoadd command to find score from coordinates
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


    // for geopos command to find coordinates from score
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

    //to get distance 
    
    public double dist(double lon1d , double lat1d , double lon2d , double lat2d){

        //to radians
        double lon1r = Math.toRadians(lon1d);
        double lon2r = Math.toRadians(lon2d);

        double v = Math.sin((lon2r - lon1r) / 2.0);
        if(v == 0.0) return latDist(lat1d , lat2d);

        double lat1r = Math.toRadians(lat1d);
        double lat2r = Math.toRadians(lat2d);

        double u  = Math.sin((lat2r - lat1r) / 2.0);

        double a = u * u  + v * v * Math.cos(lat2r) * Math.cos(lat1r);

        return 2.0 * EarthsRadius * Math.asin(Math.sqrt(a));
    }

    public double latDist(double lat1d , double lat2d){
        return EarthsRadius * Math.abs(Math.toRadians(lat2d) - Math.toRadians(lat1d));
    }
}
