import Serveur.Federation.ProtoMasterServer;

import java.io.IOException;

public class ServerLauncherCopy {
    public static void main(String[] args) {
        ProtoMasterServer master = new ProtoMasterServer();
        master.configure();
        try {
            master.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        master.listen();
        master.close();
    }
}
