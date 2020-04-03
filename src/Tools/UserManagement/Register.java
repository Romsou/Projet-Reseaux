package Tools.UserManagement;

import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class Register {
    private HashMap<SocketChannel, String> pseudos;

    public Register() {
        pseudos = new HashMap<>();
    }

    public void register(SocketChannel client, String pseudo) {
        pseudos.put(client, pseudo);
    }

    public String getClientPseudo(SocketChannel client) {
        return pseudos.get(client);
    }

}
