import Serveur.AbstractServers.AbstractDefaultSelectorServer;
import Serveur.Federation.SlaveServer;

public class ServerLauncher {
    public static void main(String[] args) {
        AbstractDefaultSelectorServer salon = new SlaveServer(12345);
        salon.listen();
        salon.close();
    }
}
