package Serveur.AbstractServers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public abstract class AbstractServer {
    public static ServerSocketChannel serverChannel;


    /**
     * Constructor of AbstractServer. Allows us to create any server we want
     * using selector
     *
     * @param port Number of the port on which to listen.
     */
    public AbstractServer(int port) {
        serverChannel = createServerChannel(port);
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


    public void close() {
        try {
            serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
