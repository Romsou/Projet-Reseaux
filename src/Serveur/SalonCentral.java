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
                String[] messageParts = message.split(" ");

                if (isLogin(client, messageParts))
                    registerLogin(client, messageParts);
                else
                    treatMessage(client, key, messageParts);
            }
        }
    }

    private void registerLogin(SocketChannel client, String[] messageParts) {
        clientPseudos.put(client, messageParts[1]);
        System.out.println(String.join(" ", messageParts));
    }

    private void treatMessage(SocketChannel client, SelectionKey key, String[] messageParts) throws IOException {
        if (isMessage(messageParts))
            System.out.println(clientPseudos.get(client) + ": " + String.join(" ", messageParts));
        else if (!isRegistered(client)) {
            sendErrorMessage("ERROR LOGIN aborting chatamu protocol\n");
            client.close();
            key.cancel();
        } else
            sendErrorMessage("ERROR chatamu\n");
    }

    private boolean isLogin(SocketChannel client, String[] loginParts) {
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
