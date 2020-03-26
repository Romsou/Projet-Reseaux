package Serveur;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SalonCentral extends AbstractSelectorServer {

    public SalonCentral(int port) {
        super(port);
    }

    /**
     * Treats acceptable keys
     *
     * @param key the key from which we get the channel to treat
     * @throws IOException
     */
    @Override
    protected void treatAcceptable(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            client = serverChannel.accept();
            client.configureBlocking(false);
            registerChannelInSelector(client, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    /**
     * Treats readable keys
     *
     * @param key the key from which we get the channel to treat
     * @throws IOException
     */
    @Override
    protected void treatReadable(SelectionKey key) throws IOException {
        if (key.isReadable()) {

            if (!client.equals(key.channel()))
                client = (SocketChannel) key.channel();

            cleanBuffer();
            int readBytes = client.read(buffer);

            if (readBytes >= 0) {
                String message = convertBufferToString();
                String[] messagePart = message.split(" ");

                if (isLogin(messagePart, client)) {
                    clientPseudos.put(client, messagePart[1]);
                    System.out.println(message);
                } else {

                    if (isMessage(messagePart))
                        System.out.println(message);
                    else if (!isRegistered(client)) {
                        sendErrorMessage("ERROR LOGIN aborting chatamu protocol\n");
                        client.close();
                        key.cancel();
                    } else if (!isMessage(messagePart) && clientPseudos.containsKey(client))
                        sendErrorMessage("ERROR chatamu\n");
                }

                /*
                // TODO: Gérer la fermeture du client sans avoir à fermer le serveur
                if (!isLogin(messagePart) && !isMessage(messagePart)) {
                    sendErrorMessage("ERROR LOGIN aborting chatamu protocol");
                    client.close();
                    System.exit(10);
                } else if (isLogin(messagePart)) {
                    clientPseudos.put(client, messagePart[1]);
                    System.out.println("nom du client: " + clientPseudos.get(client) + " Message: " + message);
                    //System.out.println(message);
                } else if (!isMessage(messagePart)) {
                    sendErrorMessage("ERROR chatamu");
                } else
                    System.out.println(message);
                 */

            }

        }
    }

    private boolean isLogin(String[] loginParts, SocketChannel client) {
        return loginParts[0].equals("LOGIN") && !clientPseudos.containsKey(client);
    }

    private boolean isMessage(String[] messageParts) {
        return messageParts[0].equals("MESSAGE") && messageParts[messageParts.length - 1].equals("envoye");
    }

    private boolean isRegistered(SocketChannel client) {
        return clientPseudos.containsKey(client);
    }

    @Override
    protected void treatWritable(SelectionKey key) {

    }
}
