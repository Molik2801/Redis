import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    List<String> parse(String input){
        String[] content = input.split("\r\n");
//        System.out.println(Arrays.toString(content));
        List<String> args = new ArrayList<>();
        args.add(content[2]);
        StringBuilder rem = new StringBuilder();
        for(int i = 3 ; i < content.length ; i++){
            rem.append(content[i]).append("\r\n");
        }
        args.add(rem.toString());
        System.out.println(args);
        return args;
    }
}
