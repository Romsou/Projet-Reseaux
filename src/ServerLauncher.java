import Serveur.AbstractServers.AbstractDefaultSelectorServer;
import Serveur.SalonCentral.SalonCentral;

public class ServerLauncher {
    public static void main(String[] args) {
        AbstractDefaultSelectorServer salon = new SalonCentral(12345);
        salon.listen();
        salon.close();
    }
}
