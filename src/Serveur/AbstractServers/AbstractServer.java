package Serveur.AbstractServers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.charset.StandardCharsets;

public abstract class AbstractServer {
    public static ServerSocketChannel serverChannel;
    public static SocketChannel client;
    public static ByteBuffer buffer;
    public static Selector selector;


    /**
     * Constructor of AbstractServer. Allows us to create any server we want
     * using selector
     *
     * @param port Number of the port on which to listen.
     */
    public AbstractServer(int port) {
        selector = openSelector();
        serverChannel = createServerChannel(port);
        buffer = ByteBuffer.allocate(1028);
        registerChannelInSelector(serverChannel, SelectionKey.OP_ACCEPT);
    }


    public abstract void listen();


    /**
     * Close the connection
     */
    public void close() {
        try {
            buffer = null;
            client.close();
            serverChannel.close();
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    /**
     * Creates and configure a server channel
     *
     * @param port the port on which to listen
     * @return A server channel that we're going to use to listen
     */
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
     * Cleans the buffer to avoid problems
     */
    protected void cleanBuffer() {
        buffer.clear();
        buffer.put(new byte[1028]);
        buffer.clear();
    }


    /**
     * Convert buffer's content into a String for further processing
     *
     * @return A string representing the content of the buffer
     */
    protected String convertBufferToString() {
        return new String(buffer.array(), StandardCharsets.UTF_8).trim();
    }


    /**
     * Sends an error message to the client
     *
     * @param message Message to send
     */
    protected void sendMessage(String message) {
        try {
            cleanBuffer();
            buffer.put(message.getBytes());
            buffer.flip();
            client.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
