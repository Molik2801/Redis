package command;

import java.io.OutputStream;
import java.util.List;

import store.RedisStore;
import store.UserData;

public class AuthCommand implements Command{

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
       String username = input.get(1);
       String password = input.get(2);

       UserData usr = store.userAuth.get(username);
       if(usr == null){
            out.write("*-1\r\n".getBytes());
            out.flush();
            return;
       }

       String hashedPass = usr.tohexString(usr.getSHA(password));
       
       if(usr.passwords.contains(hashedPass)){
           out.write("+OK\r\n".getBytes());
           out.flush();
       }
       else{
           out.write("-WRONGPASS invalid authentication\r\n".getBytes());
           out.flush();
       }

    }
    
}
