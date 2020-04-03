package Serveur.Federation;

import Serveur.ChatAmuCentral.ChatamuCentral;
import Tools.Protocol.ProtocolHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SlaveServer extends ChatamuCentral implements Runnable {
    private HashMap<SocketChannel, SocketChannel> masterServers;


    public SlaveServer(int port) {
        super(port);
        this.masterServers = new HashMap<>();
    }

    @Override
    protected void treatAcceptable(SelectionKey key) throws IOException {
        if (key.isAcceptable())
            acceptIncomingConnections(key);
    }

    @Override
    protected void treatReadable(SelectionKey key) {
        if (key.isReadable()) {
            if (!client.equals(key.channel()))
                client = (SocketChannel) key.channel();

            if (buffer.read(client) >= 0) {
                String message = buffer.convertBufferToString();
                String[] messageParts = message.split(" ");

                // Si un serveur se connecte on l'enregistre
                if (ProtocolHandler.isServerConnection(message)) {
                    System.out.println("Client connecté: " + client);
                    masterServers.put(client, client);
                    return;
                }

                // Si on reçoit un login, alors il s'agit d'un client régulier
                // On lui créer alors une file et on le met dans le registre
                if (isLogin(client, messageParts)) {
                    registerLogin(client, messageParts);
                    clientQueue.put(client, new ConcurrentLinkedQueue<>());
                    return;
                }

                if (!masterServers.keySet().isEmpty()) {
                    System.out.println("Il y a des  serveurs");
                    // Si on reçoit un message qui ne provient pas d' serveur maitre, on l'envoit au serveur maitre
                    if (ProtocolHandler.isMessage(messageParts) && !masterServers.containsKey(client)) {
                        for (SocketChannel masterServer : masterServers.keySet()) {
                            ProtocolHandler protocolHandler = new ProtocolHandler();
                            message = addPseudoToMessage(client, message);
                            System.out.println("relais du message " + message + " au serveur");
                            sendMessage(masterServer, message);
                        }
                        return;
                    }

                    // Si on reçoit un message qui provient d'un serveur maître on le transmet à tous les clients
                    if (ProtocolHandler.isMessage(messageParts) && masterServers.containsKey(client)) {
                        System.out.println("Message du serveur reçu: " + message);
                        broadcast(message);
                        return;
                    }
                }

                if (masterServers.keySet().isEmpty()) {
                    if (ProtocolHandler.isMessage(messageParts)) {
                        message = addPseudoToMessage(client, message);
                        broadcast(message);
                        return;
                    }
                }

                // Si aucune des conditions n'est remplie, alors il ne s'agit ni d'une connexion d'un serveur,
                // ni d'un login d'un client, ni d'un message valide du serveur ou des clients.
                // Si le client n'est pas enregistré, alors il s'agit d'une erreur de login.
                if (!isRegistered(client)) {
                    sendMessage(client, ProtocolHandler.ERROR_LOGIN.concat("\n"));
                    key.cancel();
                    return;
                }

                // Sinon il s'agit d'une erreur de message
                sendMessage(client, ProtocolHandler.ERROR_MESSAGE.concat("\n"));

            }
        }

    }

    @Override
    public void run() {
        this.listen();
        this.close();
    }
}
