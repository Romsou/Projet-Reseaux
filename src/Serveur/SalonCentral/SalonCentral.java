package Serveur.SalonCentral;

import Serveur.AbstractServers.AbstractDefaultSelectorServer;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class SalonCentral extends AbstractDefaultSelectorServer {


    public SalonCentral(int port) {
        super(port);
    }


    /**
     * Treats acceptable keys. Accepts incoming connections from a client.
     * Since the servers's channel is the only one registered with OP_ACCEPT,
     * It is the only one to ever use this method.
     *
     * @param key the key from which we get the channel to treat
     * @throws IOException
     */
    @Override
    protected void treatAcceptable(SelectionKey key) throws IOException {
        if (key.isAcceptable())
            acceptIncomingConnections(key);
    }


    @Override
    protected void treatWritable(SelectionKey key) {
    }


    @Override
    protected void writeMessageToClients(String message) {
        System.out.println(clientPseudos.get(client) + ": " + message);
    }

}
