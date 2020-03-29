package Serveur.Federation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


public abstract class AbstractMasterServer {

    private static final String DEFAULT_CONFIG_FILE = "src//Config/pairs.cfg";

    private ConcurrentLinkedQueue<String> master;
    private HashMap<SocketChannel, String> clientsPseudo;
    private Selector clientSelector;
    private List<SocketChannel> servers;
    private ByteBuffer buffer;


    public AbstractMasterServer() {
        this.clientsPseudo = new HashMap<>();
        this.master = new ConcurrentLinkedQueue<>();
        this.clientSelector = openSelector();
        this.servers = new ArrayList<>(15);
        this.buffer = ByteBuffer.allocate(1028);
    }

    protected Selector openSelector() {
        try {
            return Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void configure() {
        BufferedReader configReader = openConfigFile(DEFAULT_CONFIG_FILE);

        try {
            String line;
            while ((line = configReader.readLine()) != null)
                addServer(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected BufferedReader openConfigFile(String filename) {
        try {
            return new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void addServer(String line) throws IOException {
        String[] lineParts = line.split(" ");
        String host = lineParts[2];
        int port = Integer.parseInt(lineParts[3]);
        System.out.printf("Connexion à l'höte %s sur le port %d\n", host, port);
        SocketChannel server = SocketChannel.open(new InetSocketAddress(host, port));
        servers.add(server);
    }


    public void init() {
        for (SocketChannel server : servers) {
            if (sendMessage(server, "SERVERCONNECT\n") == 0)
                registerChannelInSelector(server);
        }
    }

    protected int sendMessage(SocketChannel client, String message) {
        try {

            cleanBuffer();
            buffer.put(message.getBytes());
            buffer.flip();
            return client.write(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    protected void cleanBuffer() {
        buffer.clear();
        buffer.put(new byte[1028]);
        buffer.clear();
    }

    protected void registerChannelInSelector(SocketChannel client) {
        try {

            client.register(clientSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }


    public void close() {
        try {

            clientSelector.close();

            for (SocketChannel server : servers)
                server.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
