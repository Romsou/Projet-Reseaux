package Serveur;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChatamuCentral extends AbstractSelectorServer {
    HashMap<SocketChannel, ConcurrentLinkedDeque> clientQueue;


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
    protected void treatReadable(SelectionKey key) throws IOException {
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
                else
                    treatMessage(client, key, messageParts);
            }
        }
    }

    private void treatMessage(SocketChannel client, SelectionKey key, String[] messageParts) throws IOException {
        if (isMessage(messageParts))
            writeMessage(String.join(" ", messageParts));
        else if (!isRegistered(client)) {
            sendMessage("ERROR LOGIN aborting chatamu protocol\n");
            client.close();
            key.cancel();
        } else
            sendMessage("ERROR chatamu\n");
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

    @Override
    protected void writeMessage(String message) {
        message = stripProtocolHeaders(message);
        for (SocketChannel remoteClient : clientQueue.keySet())
            clientQueue.get(remoteClient).add(clientPseudos.get(client) + ": " + message + "\n");
    }

}
