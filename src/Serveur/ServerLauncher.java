package Serveur;

public class ServerLauncher {
    public static void main(String[] args) {
        SalonCentral salon = new SalonCentral(12345);
        salon.listen();
        salon.close();
    }
}
