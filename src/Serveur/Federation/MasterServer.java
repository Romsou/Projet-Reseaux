package Serveur.Federation;

import java.io.IOException;

public class MasterServer extends AbstractMasterServer {
    public MasterServer() throws IOException {
        super(12347);
    }
}
