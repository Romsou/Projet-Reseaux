import Serveur.AbstractServers.AbstractSelectorServer;
import Serveur.Federation.SlaveServer;

public class ServerLauncher {
    public static void main(String[] args) {
        AbstractSelectorServer salon = new SlaveServer(12345);
        salon.listen();
        salon.close();
    }
}
