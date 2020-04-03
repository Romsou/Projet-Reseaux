package Serveur.Federation;

import Protocol.ProtocolHandler;
import Tools.Extended.ByteBufferExt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


public abstract class AbstractMasterServer {

    private static final String DEFAULT_CONFIG_FILE = "src//Config/pairs.cfg";

    private ConcurrentLinkedQueue<String> master;
    private HashMap<SocketChannel, String> clientsPseudos;
    private Selector clientSelector;
    private List<InetSocketAddress> remoteAddresses;
    private ByteBufferExt buffer;

    private HashMap<SocketChannel, ConcurrentLinkedQueue<String>> clientQueue;
    private ServerSocketChannel serverChannel;


    public AbstractMasterServer(int port) throws IOException {
        this.clientsPseudos = new HashMap<>();
        this.master = new ConcurrentLinkedQueue<>();
        this.clientSelector = openSelector();
        this.remoteAddresses = new ArrayList<>(15);
        this.buffer = new ByteBufferExt();

        this.clientQueue = new HashMap<>();

        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.bind(new InetSocketAddress(port));
        this.serverChannel.socket().setReuseAddress(true);
        this.serverChannel.configureBlocking(false);
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
        remoteAddresses.add(new InetSocketAddress(host, port));
    }


    public void init() throws IOException {
        for (InetSocketAddress address : remoteAddresses) {
            SocketChannel server = SocketChannel.open(address);
            server.configureBlocking(false);
            server.socket().setReuseAddress(true);
            if (sendMessage(server, "SERVERCONNECT") == 0) {
                registerChannelInSelector(server);
                clientQueue.put(server, new ConcurrentLinkedQueue<>());
            } else
                remoteAddresses.remove(server);
        }
    }

    protected int sendMessage(SocketChannel client, String message) {
        if (!ProtocolHandler.isServerConnection(message)) {
            message = new ProtocolHandler().stripProtocolHeaders(message);
            message = clientsPseudos.get(client) + ": " + message + "\n";
        }
        try {
            buffer.cleanBuffer();
            buffer.put(message.getBytes());
            buffer.flip();
            client.write(buffer.getBuffer());
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    protected void registerChannelInSelector(SocketChannel client) {
        try {
            System.out.println("Ajout client");
            client.register(clientSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            System.out.println(clientSelector.keys());
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }


    public void listen() throws IOException {
        // Enregistrement du serveur
        serverChannel.register(clientSelector, SelectionKey.OP_ACCEPT);

        while (clientSelector.select() > 0) {
            Iterator<SelectionKey> keys = clientSelector.selectedKeys().iterator();
            // Parcours des clés
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                // Si la clé valide
                if (key.isValid()) {

                    // Si la clé est acceptable, normalement le Serveur lui-même
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel client = serverChannel.accept();
                        client.configureBlocking(false);
                        client.register(clientSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        clientQueue.put(client, new ConcurrentLinkedQueue<>());
                    }


                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        System.out.println("Lecture de: " + client.socket().getPort());
                        // Lecture du buffer du client
                        buffer.cleanBuffer();
                        int readBytes = client.read(buffer.getBuffer());

                        if (readBytes >= 0) {
                            String message = buffer.convertBufferToString();
                            String[] messageParts = message.split(" ");

                            System.out.println("Message lu: " + message);

                            if (isLogin(client, messageParts))
                                registerLogin(client, messageParts);
                            else
                                treatMessage(client, key, messageParts);
                        }
                    }

                    //Si les clé est toujours correcte et inscriptible
                    if (key.isValid() && key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();

                        if (!clientQueue.get(client).isEmpty()) {
                            System.out.println("Message envoyé: " + clientQueue.get(client).peek());
                            sendMessage(client, clientQueue.get(client).poll());
                        }
                    }

                }
            }
        }
    }

    protected void registerLogin(SocketChannel client, String[] messageParts) {
        System.out.println("registering");
        clientsPseudos.put(client, messageParts[1]);
    }

    protected void treatMessage(SocketChannel client, SelectionKey key, String[] messageParts) throws IOException {
        if (ProtocolHandler.isMessage(messageParts))
            writeMessageToClients(client, String.join(" ", messageParts));
        else if (!isRegistered(client)) {
            System.out.println("Message d'erreur: " + String.join(" ", messageParts));
            sendMessage(client, "ERROR LOGIN aborting chatamu protocol\n");
            client.close();
            key.cancel();
        } else
            sendMessage(client, "ERROR chatamu\n");
    }

    // Ajoute le message lu dans les files des clients et dans master
    protected void writeMessageToClients(SocketChannel client, String message) {
        //message = new ProtocolHandler().stripProtocolHeaders(message);
        message = clientsPseudos.get(client) + ": " + message + "\n";
        master.add(message);
        for (SocketChannel remoteClient : clientQueue.keySet())
            clientQueue.get(remoteClient).add(master.peek());
        master.poll();
    }

    protected boolean isRegistered(SocketChannel client) {
        return clientsPseudos.containsKey(client);
    }


    protected boolean isLogin(SocketChannel client, String[] loginParts) {
        return ProtocolHandler.isLoginHeader(loginParts[0]) && !isRegistered(client);
    }

    public void printKeys() {
        for (SelectionKey key : clientSelector.keys()) {
            SocketChannel client = (SocketChannel) key.channel();
            try {
                System.out.printf("Adresse: %s\n", client.getRemoteAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void close() {
        try {

            clientSelector.close();
            for (SocketChannel client : clientQueue.keySet())
                client.close();
            serverChannel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
