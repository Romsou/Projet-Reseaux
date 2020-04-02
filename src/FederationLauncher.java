import Serveur.Federation.ProtoMasterServer;

import java.io.IOException;

public class FederationLauncher {
    public static void main(String[] args) throws IOException, InterruptedException {
        ProtoMasterServer server = new ProtoMasterServer();
        server.configure();
        server.init();
        server.listen();
        server.close();
    }
}


