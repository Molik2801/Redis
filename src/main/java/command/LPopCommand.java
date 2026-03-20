package command;

import java.io.OutputStream;
import java.util.Deque;
import java.util.List;

import store.RedisStore;

public class LPopCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String key = input.get(1);
        Deque<String> deque = store.list.get(key);

        if (deque == null || deque.isEmpty()) {
            out.write("$-1\r\n".getBytes());
            return;
        }

        int totalToRemove = 1;
        boolean hasCount = input.size() > 2;

        if (hasCount) {
            try {
                totalToRemove = Math.min(Integer.parseInt(input.get(2)), deque.size());
            } catch (NumberFormatException e) {
                out.write("-ERR value is not an integer\r\n".getBytes());
                return;
            }
        }

        StringBuilder resp = new StringBuilder();
        if (hasCount) {
            resp.append("*").append(totalToRemove).append("\r\n");
        }
        for (int i = 0; i < totalToRemove; i++) {
            String element = deque.removeFirst();
            resp.append("$").append(element.length()).append("\r\n").append(element).append("\r\n");
        }
        out.write(resp.toString().getBytes());
    }
    
}
