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

public class ProtoMasterServer extends ChatamuCentral implements Runnable {
    private static final String DEFAULT_CONFIG_FILE = "src//Config/pairs.cfg";

    private ConcurrentLinkedQueue<String> master;
    private List<InetSocketAddress> remoteAddresses;

    public ProtoMasterServer() {
        super(15000);
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
        remoteAddresses.add(new InetSocketAddress(host, port));
    }

    public void init() throws IOException {
        for (InetSocketAddress address : remoteAddresses) {
            SocketChannel server = SocketChannel.open(address);
            System.out.println("SUCCESS");
            server.configureBlocking(false);
            server.socket().setReuseAddress(true);
            sendMessage(server, "SERVERCONNECT\n");
            registerChannelInSelector(server, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            clientQueue.put(server, new ConcurrentLinkedQueue<>());
        }
    }

    @Override
    protected void treatAcceptable(SelectionKey key) {
        return;
    }

    @Override
    protected void treatReadable(SelectionKey key) throws IOException {
        if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            if (buffer.read(client) >= 0) {
                String message = buffer.convertBufferToString();
                System.out.println(message);
                master.add(message);
                broadcast(message);
            }
        }
    }

    @Override
    protected void treatWritable(SelectionKey key) {
        if (key.isValid() && key.isWritable()) {
            SocketChannel client = (SocketChannel) key.channel();

            if (clientQueue.containsKey(client) && !clientQueue.get(client).isEmpty()) {
                String message = clientQueue.get(client).poll();
                sendMessage(client, message);
            }
        }
    }


    @Override
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
