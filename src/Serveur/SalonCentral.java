package Serveur;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class SalonCentral {
    public static ServerSocketChannel serverChannel;
    public static SocketChannel client;
    public static ByteBuffer buffer;
    public static Selector selector;


    public SalonCentral(int port) {
        selector = openSelector();
        serverChannel = createServerChannel(port);
        buffer = ByteBuffer.allocate(1028);
        registerChannelInSelector();
        handleConnections();
    }


    public static void main(String[] args) {
        SalonCentral salon = new SalonCentral(12345);
        salon.handleConnections();
        salon.close();
    }


    private Selector openSelector() {
        try {
            return Selector.open();
        } catch (IOException e) {
            System.err.println("openSelector: Error when opening selector");
            System.exit(1);
        }
        return null;
    }


    private ServerSocketChannel createServerChannel(int port) {
        try {
            InetSocketAddress serverAddress = new InetSocketAddress(port);
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(serverAddress);
            server.socket().setReuseAddress(true);
            server.configureBlocking(false);
            return server;
        } catch (IOException e) {
            System.err.println("createServerChannel: Error");
            this.close();
            System.exit(10);
        }
        return null;
    }


    private void registerChannelInSelector() {
        try {
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            System.err.println("registerChannelInSelector: Error when registering socketchannel");
        }
    }


    public void handleConnections() {
        try {
            while (selector.select() > 0) {
                processKeys();
            }
        } catch (IOException e) {
            System.err.println("handleConnections: selector.select() error");
        }
    }


    private void processKeys() {
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();

            if (key.isValid()) {
                try {
                    treatAcceptable(key);
                    treatReadable(key);
                } catch (IOException e) {
                    System.err.println("processKeys: Error");
                }
            }
        }
    }


    private void treatAcceptable(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            client = serverChannel.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }


    private void treatReadable(SelectionKey key) throws IOException {
        if (key.isReadable()) {
            if (!client.equals(key.channel()))
                client = (SocketChannel) key.channel();

            cleanBuffer();
            int readBytes = client.read(buffer);
            if (readBytes >= 0) {
                String message = convertBufferToString();
                String[] messagePart = message.split(" ");

                // TODO: Gérer la fermeture du client sans avoir à fermer le serveur
                if (!loginIsValid(messagePart) && !messageIsValid(messagePart)) {
                    sendErrorMessage("ERROR LOGIN aborting chatamu protocol");
                    client.close();
                    System.exit(10);
                } else if (loginIsValid(messagePart)) {
                    System.out.println(message);
                } else if (!messageIsValid(messagePart)) {
                    sendErrorMessage("ERROR chatamu");
                } else
                    System.out.println(message);
            }
        }
    }


    private void cleanBuffer() {
        buffer.clear();
        buffer.put(new byte[1028]);
        buffer.clear();
    }


    private String convertBufferToString() {
        return new String(buffer.array(), StandardCharsets.UTF_8).trim();
    }


    private boolean loginIsValid(String[] loginParts) {
        return loginParts[0].equals("LOGIN");
    }


    private boolean messageIsValid(String[] messageParts) {
        return messageParts[0].equals("MESSAGE");
    }


    private void sendErrorMessage(String message) {
        try {
            cleanBuffer();
            buffer.put(message.getBytes());
            buffer.flip();
            client.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void close() {
        try {
            buffer = null;
            client.close();
            serverChannel.close();
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
