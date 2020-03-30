package Serveur.AbstractServers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public abstract class AbstractDefaultSelectorServer extends AbstractSelectorServer {


    public AbstractDefaultSelectorServer(int port) {
        super(port);
    }

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

                if (isLogin(client, messageParts))
                    registerLogin(client, messageParts);
                else
                    treatMessage(key, messageParts);
            }
        }
    }

    private void treatMessage(SelectionKey key, String[] messageParts) throws IOException {
        if (isMessage(messageParts))
            writeMessageToClients(String.join(" ", messageParts));
        else if (!isRegistered(client)) {
            sendMessage("ERROR LOGIN aborting chatamu protocol\n");
            client.close();
            key.cancel();
        } else
            sendMessage("ERROR chatamu\n");
    }

}
