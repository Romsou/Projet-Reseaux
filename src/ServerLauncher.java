import Serveur.ChatAmuCentral.ChatamuCentral;

public class ServerLauncher {
    public static void main(String[] args) {
        ChatamuCentral salon = new ChatamuCentral(12345);
        salon.listen();
        salon.close();
    }
}
