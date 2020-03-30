package Serveur.SalonCentral;

import Serveur.AbstractServers.AbstractSelectorServer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class SalonCentral extends AbstractSelectorServer {


    public SalonCentral(int port) {
        super(port);
    }


    /**
     * Treats acceptable keys. Accepts incoming connections from a client.
     * Since the servers's channel is the only one registered with OP_ACCEPT,
     * It is the only one to ever use this method.
     *
     * @param key the key from which we get the channel to treat
     * @throws IOException
     */
    @Override
    protected void treatAcceptable(SelectionKey key) throws IOException {
        if (key.isAcceptable())
            acceptIncomingConnections(key);
    }


    /**
     * Treats readable keys. Reads from the buffer and checks the content
     * of the message to see if there is any protocol violation.
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

                if (isLogin(client, messageParts)) {
                    registerLogin(client, messageParts);
                    writeMessageToClients(message);
                } else
                    treatMessage(client, key, messageParts);
            }
        }
    }


    /**
     * Treats the incoming message by applying security checks on it.
     * If a message has the good format, then it is printed, otherwise
     * we check whether the client was registered. If he was not, then, then we
     * send a login error back to the client.
     * If the client was registered, then it means it is simpply a message error.
     *
     * @param client       The client that we listen
     * @param key          The key corresponding to the client
     * @param messageParts A string array containing all parts of the message
     * @throws IOException
     */
    private void treatMessage(SocketChannel client, SelectionKey key, String[] messageParts) throws IOException {
        if (isMessage(messageParts))
            System.out.println(clientPseudos.get(client) + ": " + String.join(" ", messageParts));
        else if (!isRegistered(client)) {
            sendMessage("ERROR LOGIN aborting chatamu protocol\n");
            client.close();
            key.cancel();
        } else
            sendMessage("ERROR chatamu\n");
    }


    @Override
    protected void treatWritable(SelectionKey key) {
    }


    @Override
    protected void writeMessageToClients(String message) {
        System.out.println(clientPseudos.get(client) + ": " + message);
    }

}
