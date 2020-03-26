package Serveur;

public class ServerLauncher {
    public static void main(String[] args) {
        AbstractSelectorServer salon = new ChatamuCentral(12345);
        salon.listen();
        salon.close();
    }
}
