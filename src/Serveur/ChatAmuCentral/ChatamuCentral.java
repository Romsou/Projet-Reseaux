package Serveur.ChatAmuCentral;

import Serveur.AbstractServers.AbstractDefaultSelectorServer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatamuCentral extends AbstractDefaultSelectorServer {
    protected HashMap<SocketChannel, ConcurrentLinkedQueue<String>> clientQueue;


    public ChatamuCentral(int port) {
        super(port);
        this.clientQueue = new HashMap<>();
    }


    @Override
    protected void treatAcceptable(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            acceptIncomingConnections(key);
            clientQueue.put(client, new ConcurrentLinkedQueue<>());
        }
    }


    @Override
    protected void treatWritable(SelectionKey key) {
        if (key.isValid() && key.isWritable()) {
            if (client != null)
                client = (SocketChannel) key.channel();

            if (clientQueue.containsKey(client) && !clientQueue.get(client).isEmpty()) {
                String message = clientQueue.get(client).poll();
                System.out.println(message);
                sendMessage(client, message);
            }
        }
    }

    @Override
    protected void writeMessageToClients(String message) {
        System.out.println("Message envoyé: " + message);
        //message = protocolHandler.stripProtocolHeaders(message);
        broadcast(message);
    }

    protected void broadcast(String message) {
        for (SocketChannel remoteClient : clientQueue.keySet())
            appendToClientQueue(remoteClient, message);
    }

    protected void appendToClientQueue(SocketChannel client, String message) {
        clientQueue.get(client).add(message + "\n");
    }
}
