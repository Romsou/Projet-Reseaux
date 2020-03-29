import Serveur.Federation.MasterServer;

public class FederationLauncher {
    public static void main(String[] args) {
        MasterServer server = new MasterServer();
        server.configure();
        server.init();
        server.close();
    }
}


