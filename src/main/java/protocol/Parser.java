package protocol;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    
    public List<List<String>> parse(String input , List<List<String>> commands){
        if(input == null || input.isEmpty()){
            return new ArrayList<>();
        }

        List<String> args = new ArrayList<>();
        int ind = 0;
        int totLen = 1;

        while((totLen > 0) || (ind < input.length())){
            if(input.charAt(ind) == '*'){
                if(!args.isEmpty()){
                    commands.add(args);
                    args = new ArrayList<>();
                }

                int endPt = input.indexOf("\r\n" , ind);
                totLen = Integer.parseInt(input.substring(ind+1 , endPt));
                ind = endPt + 2;
            }
            else if(input.charAt(ind) == '$'){
                int endPt = input.indexOf("\r\n" , ind);
                int comSize = Integer.parseInt(input.substring(ind+1 , endPt));
                args.add(input.substring(endPt + 2 , endPt + 2 + comSize));
                ind = endPt + comSize + 4;
                totLen--;
            }
        }

        if(!args.isEmpty())commands.add(args);
        return commands;
    }
}

//*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\n123\r\n*3\r\n$3\r\nSET\r\n$3\r\bar\r\n$3\r\324\r\n