import Serveur.AbstractServers.AbstractSelectorServer;
import Serveur.SalonCentral.SalonCentral;

public class ServerLauncher {
    public static void main(String[] args) {
        AbstractSelectorServer salon = new SalonCentral(12345);
        salon.listen();
        salon.close();
    }
}
