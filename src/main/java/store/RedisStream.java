package store;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class RedisStream {
    public long lastTime = 0;
    public int lastSeq = 0;

    Comparator<String> idComparator = new Comparator<String>() {
        @Override
        public int compare(String id1, String id2) {
            String[] parts1 = id1.split("-");
            String[] parts2 = id2.split("-");

            long time1 = Long.parseLong(parts1[0]);
            long time2 = Long.parseLong(parts2[0]);

            if (time1 != time2) {   
                return Long.compare(time1, time2);
            } else {
                int seq1 = Integer.parseInt(parts1[1]);
                int seq2 = Integer.parseInt(parts2[1]);
                return Integer.compare(seq1, seq2);
            }
        }
    };
    public TreeMap<String , Map<String , String>> entries = new TreeMap<>(idComparator);
}
