package Serveur.AbstractServers;

import Tools.Protocol.ProtocolHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public abstract class AbstractDefaultSelectorServer extends AbstractSelectorServer {

    public AbstractDefaultSelectorServer(int port) {
        super(port);
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

            buffer.cleanBuffer();
            int readBytes = client.read(buffer.getBuffer());

            if (readBytes >= 0) {
                String message = buffer.convertBufferToString();
                String[] messageParts = message.split(" ");

                if (isLogin(client, messageParts)) {
                    registerLogin(client, messageParts);
                    return;
                } else if (ProtocolHandler.isServerConnection(message)) {
                    System.out.println("Connexion serveur détectée");
                    registerLogin(client, "LOGIN server".split(" "));
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
     * @param key          The key corresponding to the client
     * @param messageParts A string array containing all parts of the message
     * @throws IOException
     */
    protected void treatMessage(SocketChannel client, SelectionKey key, String[] messageParts) throws IOException {
        if (ProtocolHandler.isMessage(messageParts))
            writeMessageToClients(String.join(" ", messageParts));
        else if (!isRegistered(client)) {
            sendMessage(client, "ERROR LOGIN aborting chatamu protocol\n");
            client.close();
            key.cancel();
        } else
            sendMessage(client, "ERROR chatamu\n");
    }

}
