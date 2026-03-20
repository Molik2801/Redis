package command;

import store.RedisStore;
import java.io.OutputStream;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BlPopCommand implements Command {
    @Override
    public void execute(List<String> input, OutputStream outputStream, RedisStore store) throws Exception {
        String key = input.get(1);
        double timeoutSeconds = Double.parseDouble(input.get(2));
        long timeoutMillis = (long) (timeoutSeconds * 1000);

        store.waiters.putIfAbsent(key, new ConcurrentLinkedQueue<>());
        ConcurrentLinkedQueue<CompletableFuture<String>> queue = store.waiters.get(key);
        
        CompletableFuture<String> future = new CompletableFuture<>();
        boolean immediatePop = false;
        String poppedElement = null;

        synchronized (queue) {
            Deque<String> deque = store.list.get(key);
            if (deque != null && !deque.isEmpty()) {
                poppedElement = deque.removeFirst();
                immediatePop = true;
            } else {
                queue.add(future);
            }
        }

        try {
            String element;
            if (immediatePop) {
                element = poppedElement;
            } else {
                if(timeoutMillis > 0){
                    element = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
                } else {
                    element = future.get();
                }
            }

            String response = "*2\r\n$" + key.length() + "\r\n" + key + "\r\n$" + element.length() + "\r\n" + element + "\r\n";
            outputStream.write(response.getBytes());
            outputStream.flush();

        } catch (TimeoutException e) {
            future.cancel(false);
            synchronized (queue) {
                queue.remove(future);
            }
            outputStream.write("*-1\r\n".getBytes());
        } catch (Exception e) {
            outputStream.write("-ERR BLPOP interrupted\r\n".getBytes());
            Thread.currentThread().interrupt();
        }
    }
}