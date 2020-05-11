package Serveur.Federation;

import Serveur.AbstractServers.TemplateServer;
import Tools.ConfigParser.ConfigParser;
import Tools.Extended.SocketChannelExt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MasterServer extends TemplateServer {
    private static final String DEFAULT_CONFIG_FILE = "src//Config/pairs.cfg";

    private ConcurrentLinkedQueue<String> masterQueue;
    private List<InetSocketAddress> remoteAddresses;


    public MasterServer() {
        super(15000);
        this.masterQueue = new ConcurrentLinkedQueue<>();
        this.remoteAddresses = new LinkedList<>();
    }


    public void configure() {
        ConfigParser configParser = new ConfigParser(DEFAULT_CONFIG_FILE);
        String[] configFileContent = configParser.read();

        for (String line : configFileContent)
            addServer(line);
    }

    private void addServer(String line) {
        String[] lineParts = line.split(" ");
        String host = lineParts[2];
        int port = Integer.parseInt(lineParts[3]);
        remoteAddresses.add(new InetSocketAddress(host, port));
    }


    public void init() throws IOException {
        for (InetSocketAddress address : remoteAddresses) {
            SocketChannelExt server = new SocketChannelExt();
            server.setSocketChannel(SocketChannel.open(address));
            System.out.println("SUCCESS: server at remote address " + address.getAddress() + " is connected");
            server.configureBlocking(false);
            server.setReuseAddress(true);
            communicator.send(server, "SERVERCONNECT\n");
            server.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            clientQueues.addClientQueue(server);
        }
    }

    @Override
    protected void acceptKey(SelectionKey key) {
    }

    @Override
    protected void readKey(SelectionKey key) {
        SocketChannelExt client = new SocketChannelExt();
        client.getServerFromKey(key);

        if (communicator.hasReceived(client)) {
            String message = communicator.receive();
            System.out.println(message);
            masterQueue.add(message);
            clientQueues.broadcast(message);
        }
    }

    @Override
    protected void writeKey(SelectionKey key) {
        SocketChannelExt client = new SocketChannelExt();
        client.getServerFromKey(key);

        if (clientQueues.contains(client) && !clientQueues.queueIsEmpty(client)) {
            String message = clientQueues.pollPendingMessage(client);
            communicator.send(client, message + "\n");
        }
    }


    public void run() {
        this.configure();
        try {
            this.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.listen();
//        this.close();
    }
}
