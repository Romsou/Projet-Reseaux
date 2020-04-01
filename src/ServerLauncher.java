import Serveur.Federation.SlaveServer;

public class ServerLauncher {
    public static void main(String[] args) {
        //AbstractDefaultSelectorServer salon = new ChatamuCentral(12345);
        Runnable server = new SlaveServer(12345);


        Thread slave = new Thread(server);
        slave.start();
        //salon.listen();
        //salon.close();
    }
}
