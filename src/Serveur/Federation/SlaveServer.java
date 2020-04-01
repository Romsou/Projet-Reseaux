package Serveur.Federation;

import Protocol.ProtocolHandler;
import Serveur.ChatAmuCentral.ChatamuCentral;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SlaveServer extends ChatamuCentral {
    private HashMap<SocketChannel, SocketChannel> masterServers;


    public SlaveServer(int port) {
        super(port);
    }

    @Override
    protected void treatAcceptable(SelectionKey key) throws IOException {
        if (key.isAcceptable())
            acceptIncomingConnections(key);
    }

    @Override
    protected void treatReadable(SelectionKey key) {
        if (key.isReadable()) {
            if (!client.equals(key.channel()))
                client = (SocketChannel) key.channel();

            if (buffer.read(client) >= 0) {
                String message = buffer.convertBufferToString();
                String[] messageParts = message.split(" ");

                if (ProtocolHandler.isServerConnection(message)) {
                    masterServers.put(client, client);
                    return;
                }

                if (isLogin(client, messageParts)) {
                    registerLogin(client, messageParts);
                    clientQueue.put(client, new ConcurrentLinkedQueue<>());
                    return;
                }

                if (ProtocolHandler.isMessage(messageParts) && !masterServers.containsKey(client)) {
                    for (SocketChannel masterServer : masterServers.keySet())
                        sendMessage(masterServer, message);
                }


                if (ProtocolHandler.isMessage(messageParts) && masterServers.containsKey(client)) {
                    broadcast(message);
                    return;
                }

                if (!isRegistered(client)) {
                    sendMessage(client, ProtocolHandler.ERROR_LOGIN.concat("\n"));
                    key.cancel();
                    return;
                }

                sendMessage(client, ProtocolHandler.ERROR_MESSAGE.concat("\n"));

            }
        }

    }
}
