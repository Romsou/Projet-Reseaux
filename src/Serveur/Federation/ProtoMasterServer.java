package Serveur.Federation;

import Protocol.ProtocolHandler;
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
        System.out.printf("Connexion à l'höte %s sur le port %d\n", host, port);
        remoteAddresses.add(new InetSocketAddress(host, port));
    }

    public void init() throws IOException {
        for (InetSocketAddress address : remoteAddresses) {
            SocketChannel server = SocketChannel.open(address);
            server.configureBlocking(false);
            server.socket().setReuseAddress(true);

            sendMessage(server, "SERVERCONNECT\n");
            registerChannelInSelector(server, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            this.clientQueue.put(server, new ConcurrentLinkedQueue<>());
        }
    }

    @Override
    protected void treatReadable(SelectionKey key) throws IOException {
        if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();

            int readBytes = buffer.read(client);
            if (readBytes > 0) {
                String message = buffer.convertBufferToString();
                String[] messageParts = message.split(" ");

                if (isLogin(client, messageParts)) {
                    registerLogin(client, messageParts);
                    return;
                }

                if (ProtocolHandler.isMessage(messageParts)) {
                    master.add(addPseudoToMessage(client, message));
                    broadcast(master.peek());
                    return;
                }

                if (!isRegistered(client)) {
                    //System.out.println("Message d'erreur lu: " + String.join(" ", messageParts));
                    sendMessage(client, ProtocolHandler.ERROR_LOGIN.concat("\n"));
                    client.close();
                    key.cancel();
                    return;
                }

                sendMessage(client, ProtocolHandler.ERROR_MESSAGE.concat("\n"));
            }
        }
    }

    @Override
    protected void treatWritable(SelectionKey key) {
        if (key.isValid() && key.isWritable()) {
            SocketChannel client = (SocketChannel) key.channel();

            if (!clientQueue.get(client).isEmpty()) {
                System.out.println("Message ecrit: " + clientQueue.get(client).peek());
                sendMessage(client, clientQueue.get(client).poll());
            }
        }
    }

    @Override
    protected void processKeys() {
        super.processKeys();
        master.poll();
    }

}
