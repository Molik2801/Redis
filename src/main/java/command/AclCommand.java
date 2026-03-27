package command;

import java.io.OutputStream;
import java.util.List;
import store.RedisStore;
import store.UserData;

public class AclCommand implements Command {

    @Override
    public void execute(List<String> input, OutputStream out, RedisStore store) throws Exception {
        String command = input.get(1);
        StringBuilder resp = new StringBuilder();

        if(command.toUpperCase().equals("WHOAMI")){
            if(store.isAuthenticated.get(out) == true) resp.append("$7\r\ndefault\r\n");
            else resp.append("-NOAUTH Authentication required\r\n");
        }
        if(command.toUpperCase().equals("GETUSER")){
            String user = input.get(2);
            UserData usr = store.userAuth.get(user);
            
            resp.append("*4\r\n");
            resp.append("$5\r\nflags\r\n");
            if(usr == null || (usr != null && usr.nopass)){
                resp.append("*1\r\n");
                resp.append("$6\r\nnopass\r\n");
                resp.append("$9\r\npasswords\r\n");
                resp.append("*0\r\n");
            }
            else {
                resp.append("*0\r\n");
                resp.append("$9\r\npasswords\r\n");
                resp.append("*" + usr.passwords.size() + "\r\n");
                for(String entry : usr.passwords){
                    resp.append("$" + entry.length() + "\r\n" + entry + "\r\n");
                }
            }
        
        }
        if(command.toUpperCase().equals("SETUSER")){
            String user = input.get(2);
            String modifyCommand = input.get(3);

            store.userAuth.computeIfAbsent(user, k -> new UserData());
            UserData usr = store.userAuth.get(user);

            if(modifyCommand.charAt(0) == '>'){
                String password = modifyCommand.substring(1);
                String hashedPassword = usr.tohexString(usr.getSHA(password));

                usr.nopass = false;
                usr.passwords.add(hashedPassword);
                resp.append("+OK\r\n");
            }
        }

        out.write(resp.toString().getBytes());
        out.flush();
    }
    
}
