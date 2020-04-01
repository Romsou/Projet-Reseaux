package Serveur.AbstractServers;

import Protocol.ProtocolHandler;
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
    protected static ProtocolHandler protocolHandler;


    public AbstractSelectorServer(int port) {
        super(port);
        selector = openSelector();
        buffer = new ByteBufferExt();
        this.clientPseudos = new HashMap<>();
        protocolHandler = new ProtocolHandler();
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


    protected void registerLogin(SocketChannel client, String[] messageParts) {
        clientPseudos.put(client, messageParts[1]);
    }


    protected boolean isRegistered(SocketChannel client) {
        return clientPseudos.containsKey(client);
    }


    protected boolean isLogin(SocketChannel client, String[] loginParts) {
        return ProtocolHandler.isLoginHeader(loginParts[0]) && !isRegistered(client);
    }


    /**
     * Close the connection
     */
    @Override
    public void close() {
        try {
            buffer = null;
            if (client != null)
                client.close();
            if (selector != null)
                selector.close();
            super.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sends an error message to the client
     *
     * @param message Message to send
     */
    protected void sendMessage(SocketChannel client, String message) {
        try {
            buffer.cleanBuffer();
            buffer.put(message.getBytes());
            buffer.flip();
            client.write(buffer.getBuffer());
        } catch (IOException e) {
            try {
                client.close();
            } catch (IOException ex) {
                System.err.println("sendMessage: Problème de fermeture avec le client");
            }
            System.out.println("Connexion terminée");
        }
    }


    protected abstract void writeMessageToClients(String message);


    protected abstract void treatReadable(SelectionKey key) throws IOException;

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


    public String addPseudoToMessage(SocketChannel client, String message) {
        return clientPseudos.get(client) + ": " + message;
        /*
        String[] messageParts = message.split(" ");
        String messageWithPseudo = clientPseudos.get(client) + ":" + messageParts[0];
        for (int i = 1; i < messageParts.length; i++) {
            messageWithPseudo += " " + messageParts[i];
        }
        return messageWithPseudo;
        */
    }

    protected abstract void treatAcceptable(SelectionKey key) throws IOException;


    protected abstract void treatWritable(SelectionKey key) throws IOException;


}
