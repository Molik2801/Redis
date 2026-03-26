package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;
import store.RedisZSet;
import store.ZSetEntry;

public class ZRangeCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        int start = Integer.parseInt(input.get(2));
        int end = Integer.parseInt(input.get(3));

        RedisZSet zSet = store.sortedSet.get(key);

        if(zSet == null){
            out.write("*0\r\n".getBytes());
            out.flush();
            return;
        }

        int totalSize = zSet.memberScores.size();
        if(start < 0) start = Math.max(0, totalSize + start);
        if(end < 0) end = Math.max(0, totalSize + end);

        if((start > end) || start >= totalSize){
            out.write("*0\r\n".getBytes());
            out.flush();
            return;
        }


        end = Math.min(end , totalSize-1);

        int totalMembers = end - start + 1;

        StringBuilder resp = new StringBuilder();
        resp.append("*" + totalMembers + "\r\n");

        int rank = 0;
        for(ZSetEntry entry : zSet.orderedSet){
            if(rank <= end && rank >= start){
                resp.append("$" + entry.member.length() + "\r\n" + entry.member + "\r\n");
            }
            if(rank == end)break;
            rank++;
        }

        out.write(resp.toString().getBytes());
        out.flush();
    }
    
}
