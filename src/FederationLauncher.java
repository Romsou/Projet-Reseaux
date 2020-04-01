import Serveur.Federation.ProtoMasterServer;

import java.io.IOException;

public class FederationLauncher {
    public static void main(String[] args) throws IOException {
        ProtoMasterServer server = new ProtoMasterServer(12347);
        server.configure();
        server.init();
        //server.printKeys();
        server.listen();
        server.close();
    }
}


