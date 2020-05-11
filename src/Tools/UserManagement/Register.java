package Tools.UserManagement;

import Tools.Extended.SocketChannelExt;

import java.util.HashMap;

public class Register {
    public HashMap<SocketChannelExt, String> pseudos;


    public Register() {
        pseudos = new HashMap<>();
    }


    public void register(SocketChannelExt client, String pseudo) {
        pseudos.put(client, pseudo);
    }


    public String getClientPseudo(SocketChannelExt client) {
        return pseudos.get(client);
    }


    public boolean containsKey(SocketChannelExt client) {
        return pseudos.containsKey(client);
    }
}
