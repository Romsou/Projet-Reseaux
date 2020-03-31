package Serveur.ChatAmuCentral;

import Serveur.AbstractServers.AbstractDefaultSelectorServer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChatamuCentral extends AbstractDefaultSelectorServer {
    private HashMap<SocketChannel, ConcurrentLinkedDeque> clientQueue;


    public ChatamuCentral(int port) {
        super(port);
        this.clientQueue = new HashMap<>();
    }


    @Override
    protected void treatAcceptable(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            acceptIncomingConnections(key);
            clientQueue.put(client, new ConcurrentLinkedDeque());
        }
    }


    @Override
    protected void treatWritable(SelectionKey key) {
        if (key.isValid() && key.isWritable()) {
            if (client != null)
                client = (SocketChannel) key.channel();

            if (!clientQueue.get(client).isEmpty())
                sendMessage((String) clientQueue.get(client).pop());
        }
    }


    @Override
    protected void writeMessageToClients(String message) {
        System.out.println("Message envoyé: " + message);
        message = protocolHandler.stripProtocolHeaders(message);
        for (SocketChannel remoteClient : clientQueue.keySet())
            clientQueue.get(remoteClient).add(clientPseudos.get(client) + ": " + message + "\n");
    }


}
