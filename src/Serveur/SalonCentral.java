package Serveur;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SalonCentral extends AbstractServer {

    public SalonCentral(int port) {
        super(port);
    }

    @Override
    public void listen() {
        try {
            while (selector.select() > 0) {
                processKeys();
            }
        } catch (IOException e) {
            System.err.println("handleConnections: selector.select() error");
        }
    }

    /**
     * Process the keys selected by the selector
     */
    private void processKeys() {
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();

            if (key.isValid()) {
                try {
                    treatAcceptable(key);
                    treatReadable(key);
                } catch (IOException e) {
                    System.err.println("processKeys: Error");
                }
            }
        }
    }

    /**
     * Treats acceptable keys
     *
     * @param key the key from which we get the channel to treat
     * @throws IOException
     */
    private void treatAcceptable(SelectionKey key) throws IOException {
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
    private void treatReadable(SelectionKey key) throws IOException {
        if (key.isReadable()) {

            if (!client.equals(key.channel()))
                client = (SocketChannel) key.channel();

            cleanBuffer();
            int readBytes = client.read(buffer);

            if (readBytes >= 0) {
                String message = convertBufferToString();
                String[] messagePart = message.split(" ");

                // TODO: Gérer la fermeture du client sans avoir à fermer le serveur
                if (!loginIsValid(messagePart) && !messageIsValid(messagePart)) {
                    sendErrorMessage("ERROR LOGIN aborting chatamu protocol");
                    client.close();
                    System.exit(10);
                } else if (loginIsValid(messagePart)) {
                    System.out.println(message);
                } else if (!messageIsValid(messagePart)) {
                    sendErrorMessage("ERROR chatamu");
                } else
                    System.out.println(message);
            }
        }
    }

    /**
     * Checks if a login message has the correct form
     *
     * @param loginParts The parts of the login message
     * @return A boolean that indicates if the login message is valid
     */
    private boolean loginIsValid(String[] loginParts) {
        return loginParts[0].equals("LOGIN");
    }

    /**
     * Checks if a message corresponds to the chatamu protocol
     *
     * @param messageParts The parts of the message to check
     * @return A boolean that indicates if the message is valid
     */
    private boolean messageIsValid(String[] messageParts) {
        return messageParts[0].equals("MESSAGE");
    }

}
