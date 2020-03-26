package Serveur;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

public abstract class AbstractSelectorServer extends AbstractServer {
    HashMap<SocketChannel, String> clientPseudos;


    public AbstractSelectorServer(int port) {
        super(port);
        this.clientPseudos = new HashMap<>();
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
    protected void processKeys() {
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();

            if (key.isValid()) {
                try {
                    treatAcceptable(key);
                    treatReadable(key);
                    treatWritable(key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected abstract void treatReadable(SelectionKey key) throws IOException;

    protected abstract void treatAcceptable(SelectionKey key) throws IOException;

    protected abstract void treatWritable(SelectionKey key) throws IOException;

}
