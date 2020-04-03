import Serveur.ChatAmuCentral.ChatamuCentralV2;

public class ServerLauncher {
    public static void main(String[] args) {
        ChatamuCentralV2 salon = new ChatamuCentralV2(12345);
        salon.listen();
        salon.close();
    }
}
