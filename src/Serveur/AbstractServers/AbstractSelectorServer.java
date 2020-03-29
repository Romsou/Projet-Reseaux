package Serveur.AbstractServers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

public abstract class AbstractSelectorServer extends AbstractServer {
    protected HashMap<SocketChannel, String> clientPseudos;


    public AbstractSelectorServer(int port) {
        super(port);
        this.clientPseudos = new HashMap<>();
    }


    protected void acceptIncomingConnections(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        client = serverChannel.accept();
        client.configureBlocking(false);
        registerChannelInSelector(client, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }


    @Override
    public void listen() {
        try {
            while (selector.select() > 0) {
                processKeys();
            }
        } catch (IOException e) {
            System.err.println("handleConnections: selector.select() error");
        }
    }


    /**
     * Process the keys selected by the selector
     */
    protected void processKeys() {
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();

            if (key.isValid()) {
                treatKey(key);
            }
        }
    }


    protected void treatKey(SelectionKey key) {
        try {
            treatAcceptable(key);
            treatReadable(key);
            treatWritable(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected String stripProtocolHeaders(String message) {
        String[] messageParts = message.split(" ");
        if (messageParts[0].equals("LOGIN"))
            return message.substring("LOGIN".length());
        else if (messageParts[0].equals("MESSAGE"))
            return message.substring("MESSAGE".length(), message.length() - "envoye".length()).strip();
        else
            return null;
    }


    protected void registerLogin(SocketChannel client, String[] messageParts) {
        clientPseudos.put(client, messageParts[1]);
    }


    protected boolean isMessage(String[] messageParts) {
        return messageParts[0].equals("MESSAGE") && messageParts[messageParts.length - 1].equals("envoye");
    }


    protected boolean isRegistered(SocketChannel client) {
        return clientPseudos.containsKey(client);
    }


    protected boolean isLogin(SocketChannel client, String[] loginParts) {
        return loginParts[0].equals("LOGIN") && !clientPseudos.containsKey(client);
    }


    protected abstract void writeMessage(String message);


    protected abstract void treatReadable(SelectionKey key) throws IOException;


    protected abstract void treatAcceptable(SelectionKey key) throws IOException;


    protected abstract void treatWritable(SelectionKey key) throws IOException;


}
