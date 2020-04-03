package Serveur.ChatAmuCentral;

import Tools.Communication.IOCommunicator;
import Tools.Extended.SelectorExt;
import Tools.Extended.ServerSocketChannelExt;
import Tools.Extended.SocketChannelExt;
import Tools.Protocol.ProtocolHandler;
import Tools.UserManagement.ClientQueueManager;
import Tools.UserManagement.Register;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Iterator;

public class ChatamuCentralV2 {
    public static ServerSocketChannelExt serverSocketChannel;
    public SelectorExt selector;
    public Register register;
    public IOCommunicator communicator;
    public ClientQueueManager clientQueues;
    public HashMap<SocketChannelExt, SocketChannelExt> masters;

    public ChatamuCentralV2(int port) {
        serverSocketChannel = new ServerSocketChannelExt();
        serverSocketChannel.bind(port);

        selector = new SelectorExt();
        register = new Register();
        communicator = new IOCommunicator();
        clientQueues = new ClientQueueManager();
        masters = new HashMap<>();
    }

    public void listen() {
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (selector.select() > 0) {
            Iterator<SelectionKey> keys = selector.getSelectedKeysIterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                processKey(key);
            }
        }
    }

    private void processKey(SelectionKey key) {
        if (key.isValid() && key.isAcceptable())
            acceptKey(key);
        if (key.isValid() && key.isReadable())
            readKey(key);
        if (key.isValid() && key.isWritable())
            writeKey(key);
    }

    private void acceptKey(SelectionKey key) {
        serverSocketChannel.getServerFromKey(key);
        SocketChannelExt client = new SocketChannelExt();
        client.setSocketChannel(serverSocketChannel.accept());
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void readKey(SelectionKey key) {
        SocketChannelExt client = new SocketChannelExt();
        client.getServerFromKey(key);

        if (communicator.hasReceived(client)) {
            String message = communicator.receive();
            String[] messageParts = message.split(" ");

            //TODO: A effacer
            System.out.println(message);

            // Si un serveur se connecte on l'enregistre
            if (ProtocolHandler.isServerConnection(message)) {
                System.out.println("Client connecté: " + client);
                masters.put(client, client);
                return;
            }

            // Si on reçoit un login, alors il s'agit d'un client régulier
            // On lui créer alors une file et on le met dans le registre
            if (ProtocolHandler.isLoginHeader(messageParts[0]) && !register.containsKey(client)) {
                register.register(client, messageParts[1]);
                clientQueues.addClientQueue(client);
                return;
            }

            if (!masters.keySet().isEmpty()) {
                System.out.println("Il y a des  serveurs");
                // Si on reçoit un message qui ne provient pas d' serveur maitre, on l'envoit au serveur maitre
                if (ProtocolHandler.isMessage(messageParts) && !masters.containsKey(client)) {
                    for (SocketChannelExt master : masters.keySet()) {
                        message = addPseudoToMessage(client, message);
                        System.out.println("relais du message " + message + " au serveur");
                        communicator.send(master, message);
                    }
                    return;
                }

                // Si on reçoit un message qui provient d'un serveur maître on le transmet à tous les clients
                if (ProtocolHandler.isMessage(messageParts) && masters.containsKey(client)) {
                    System.out.println("Message du serveur reçu: " + message);
                    clientQueues.broadcast(message);
                    return;
                }
            }

            if (masters.keySet().isEmpty()) {
                if (ProtocolHandler.isMessage(messageParts)) {
                    message = addPseudoToMessage(client, message);
                    clientQueues.broadcast(message);
                    return;
                }
            }

            // Si aucune des conditions n'est remplie, alors il ne s'agit ni d'une connexion d'un serveur,
            // ni d'un login d'un client, ni d'un message valide du serveur ou des clients.
            // Si le client n'est pas enregistré, alors il s'agit d'une erreur de login.
            if (!register.containsKey(client)) {
                communicator.send(client, ProtocolHandler.ERROR_LOGIN.concat("\n"));
                key.cancel();
                return;
            }

            // Sinon il s'agit d'une erreur de message
            communicator.send(client, ProtocolHandler.ERROR_MESSAGE.concat("\n"));

        }


    }

    public String addPseudoToMessage(SocketChannelExt client, String message) {
        ProtocolHandler protocolHandler = new ProtocolHandler();
        message = protocolHandler.stripProtocolHeaders(message);
        message = register.getClientPseudo(client) + ": " + message;
        message = protocolHandler.addMessageHeaders(message);
        return message + "\n";
    }

    private void writeKey(SelectionKey key) {
        SocketChannelExt client = new SocketChannelExt();
        client.getServerFromKey(key);

        if (clientQueues.contains(client) && !clientQueues.queueIsEmpty(client)) {
            String message = clientQueues.pollPendingMessage(client);
            communicator.send(client, message);
        }
    }

    public void close() {
        serverSocketChannel.close();
        selector.close();
    }
}
