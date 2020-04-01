package Serveur.Federation;

import Serveur.ChatAmuCentral.ChatamuCentral;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProtoMasterServer extends ChatamuCentral {
    private static final String DEFAULT_CONFIG_FILE = "src//Config/pairs.cfg";

    private ConcurrentLinkedQueue<String> master;
    private List<InetSocketAddress> remoteAddresses;
    private Thread slaveServer;

    public ProtoMasterServer(int port) {
        super(port);
        this.master = new ConcurrentLinkedQueue<>();
        this.remoteAddresses = new LinkedList<>();
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

        if (lineParts[0].equals("master"))
            slaveServer = new Thread(new SlaveServer(port));
        else
            remoteAddresses.add(new InetSocketAddress(host, port));
    }

    public void init() throws IOException {
        slaveServer.start();

        for (InetSocketAddress address : remoteAddresses) {
            SocketChannel server = SocketChannel.open(address);
            server.configureBlocking(false);
            server.socket().setReuseAddress(true);

            sendMessage(server, "SERVERCONNECT\n");
            registerChannelInSelector(server, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            this.clientQueue.put(server, new ConcurrentLinkedQueue<>());
        }
    }


}
