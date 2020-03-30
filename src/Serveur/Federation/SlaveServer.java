package Serveur.Federation;

import Serveur.AbstractServers.AbstractSelectorServer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SlaveServer extends AbstractSelectorServer {
    HashMap<SocketChannel, ConcurrentLinkedDeque> clientQueue;
    private SocketChannel masterServer;

    public SlaveServer(int port) {
        super(port);
        this.masterServer = new ArrayList<>(10);
        this.clientQueue = new HashMap<>();
    }

    @Override
    protected void writeMessageToClients(String message) {
        message = stripProtocolHeaders(message);
        for (SocketChannel remoteClient : clientQueue.keySet())
            clientQueue.get(remoteClient).add(clientPseudos.get(client) + ": " + message + "\n");
    }

    @Override
    protected void treatReadable(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            acceptIncomingConnections(key);
            clientQueue.put(client, new ConcurrentLinkedDeque());
        }
    }

    @Override
    protected void treatAcceptable(SelectionKey key) throws IOException {
        if (key.isReadable()) {
            if (!client.equals(key.channel()))
                client = (SocketChannel) key.channel();

            cleanBuffer();
            int readBytes = client.read(buffer);

            if (readBytes >= 0) {
                String message = convertBufferToString();
                String[] messageParts = message.split(" ");

                if (isLogin(client, messageParts))
                    registerLogin(client, messageParts);
                else if (isServer(client, messageParts)) {
                    System.out.println("Serveur connecté" + client.getRemoteAddress());
                    masterServer = client;
                } else
                    treatMessage(client, key, messageParts);
            }
        }

    }

    private void treatMessage(SocketChannel client, SelectionKey key, String[] messageParts) throws IOException {
        if (isMessage(messageParts) && isMasterServer(client))
            writeMessageToClients(String.join(" ", messageParts));
        else if (isMessage(messageParts)) {
            String message = (String.join(" ", messageParts));
            sendMessage(masterServer, message);
        } else if (!isRegistered(client)) {
            sendMessage("ERROR LOGIN aborting chatamu protocol\n");
            client.close();
            key.cancel();
        } else
            sendMessage("ERROR chatamu\n");
    }

    protected boolean isMasterServer(SocketChannel client) {
        return client.equals(masterServer);
    }

    protected boolean isServer(SocketChannel client, String[] messageParts) {
        return messageParts[0].equals("SERVERCONNECT") && messageParts.length == 1;
    }

    @Override
    protected void treatWritable(SelectionKey key) throws IOException {
        if (key.isWritable()) {
            if (client != null)
                client = (SocketChannel) key.channel();

            if (!clientQueue.get(client).isEmpty())
                sendMessage((String) clientQueue.get(client).pop());
        }
    }


}
