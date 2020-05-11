package Serveur.ChatAmuCentral;

import Serveur.AbstractServers.TemplateServer;
import Tools.Extended.ErrorCodes;
import Tools.Extended.SocketChannelExt;
import Tools.Protocol.ProtocolHandler;

import java.nio.channels.SelectionKey;
import java.util.HashMap;

public class ChatamuCentral extends TemplateServer {
    public HashMap<SocketChannelExt, SocketChannelExt> masters;


    public ChatamuCentral(int port) {
        super(port);
        masters = new HashMap<>();
    }


    protected void acceptKey(SelectionKey key) {
        serverSocketChannel.getServerFromKey(key);
        SocketChannelExt client = new SocketChannelExt();
        client.setSocketChannel(serverSocketChannel.accept());
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }


    protected void readKey(SelectionKey key) {
        SocketChannelExt client = new SocketChannelExt();
        client.getServerFromKey(key);

        if (communicator.hasReceived(client)) {
            String message = communicator.receive();
            String[] messageParts = message.split(" ");


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


    private String addPseudoToMessage(SocketChannelExt client, String message) {
        ProtocolHandler protocolHandler = new ProtocolHandler();
        message = protocolHandler.stripProtocolHeaders(message);
        message = register.getClientPseudo(client) + ": " + message;
        message = protocolHandler.addMessageHeaders(message);
        return message;
    }


    protected void writeKey(SelectionKey key) {
        SocketChannelExt client = new SocketChannelExt();
        client.getServerFromKey(key);

        if (clientQueues.contains(client) && !clientQueues.queueIsEmpty(client)) {
            String message = clientQueues.pollPendingMessage(client);
            System.out.println("Envoie du message: " + message);
            int connectionStatus = communicator.send(client, message + '\n');

            if (connectionStatus == ErrorCodes.SENDING_FAIL.getCode())
                clientQueues.remove(client);
        }
    }
}
