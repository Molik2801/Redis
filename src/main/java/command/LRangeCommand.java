package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;

public class LRangeCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        int l = Integer.parseInt(input.get(2));
        int r = Integer.parseInt(input.get(3));
        String listName = input.get(1);
        StringBuilder respBulk = new StringBuilder();

        if(store.list.containsKey(listName)){
            if(l < 0){
                l = Math.max(0 , store.list.get(listName).size() + l);
            }
            if(r < 0){
                r = Math.max(0 , store.list.get(listName).size() + r);
            }
            int size = Math.min(r , store.list.get(listName).size() - 1) - Math.max(0 , l) + 1;
            respBulk.append("*").append(size).append("\r\n");
            System.out.println(size);
            int lr = Math.max(0 , l);
            int rr = Math.min(r , store.list.get(listName).size() - 1);
            List<String> range = store.list.get(listName).stream().skip(lr).limit(rr - lr + 1).toList();
            System.out.println(range);
            for(int i = 0 ; i < range.size() ; i++){
                String element = range.get(i);
                System.out.println(i + " " + element);
                respBulk.append("$").append(element.length()).append("\r\n").append(element).append("\r\n");
            }
        }
        else{
            respBulk.append("*0\r\n");
        }
        out.write(respBulk.toString().getBytes());
    }
    
}
