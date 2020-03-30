package Serveur.AbstractServers;

import Tools.Network.ByteBufferExt;

import java.io.IOException;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.HashMap;
import java.util.Iterator;

public abstract class AbstractSelectorServer extends AbstractServer {
    protected HashMap<SocketChannel, String> clientPseudos;
    public static SocketChannel client;
    public static ByteBufferExt buffer;
    public static Selector selector;


    public AbstractSelectorServer(int port) {
        super(port);
        selector = openSelector();
        buffer = new ByteBufferExt();
        this.clientPseudos = new HashMap<>();
    }

    /**
     * Opens and returns a selector
     *
     * @return A selector used to create our multi-client server
     */
    private Selector openSelector() {
        try {
            return Selector.open();
        } catch (IOException e) {
            System.err.println("openSelector: Error when opening selector");
            System.exit(1);
        }
        return null;
    }


    public void listen() {
        try {
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (selector.select() > 0) {
                processKeys();
            }
        } catch (IOException e) {
            System.err.println("handleConnections: selector.select() error");
        }
    }


    protected void acceptIncomingConnections(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        client = serverChannel.accept();
        client.configureBlocking(false);
        registerChannelInSelector(client, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * Registers a channel in a selector
     *
     * @param channel        the channel to register
     * @param selectionkeyOP The Op under which to register the channel
     */
    protected void registerChannelInSelector(AbstractSelectableChannel channel, int selectionkeyOP) {
        try {
            channel.register(selector, selectionkeyOP);
        } catch (ClosedChannelException e) {
            System.err.println("registerChannelInSelector: Error when registering socketchannel");
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


    /**
     * Close the connection
     */
    @Override
    public void close() {
        try {
            buffer = null;
            client.close();
            super.close();
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sends an error message to the client
     *
     * @param message Message to send
     */
    protected void sendMessage(String message) {
        try {
            buffer.cleanBuffer();
            buffer.put(message.getBytes());
            buffer.flip();
            client.write(buffer.getBuffer());
        } catch (IOException e) {
            try {
                client.close();
            } catch (IOException ex) {
                System.err.println("sendMessage: Problème de fermeture avec  le client");
            }
            System.out.println("Connexion terminée");
        }
    }


    protected abstract void writeMessageToClients(String message);


    protected abstract void treatReadable(SelectionKey key) throws IOException;


    protected abstract void treatAcceptable(SelectionKey key) throws IOException;


    protected abstract void treatWritable(SelectionKey key) throws IOException;


}
