import Serveur.Federation.MasterServer;

import java.io.IOException;

public class FederationLauncher {
    public static void main(String[] args) throws IOException {
        MasterServer server = new MasterServer();
        server.configure();
        server.init();
        server.listen();
        server.close();
    }
}


