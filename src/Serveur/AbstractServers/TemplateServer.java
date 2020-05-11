package Serveur.AbstractServers;

import Tools.Communication.IOCommunicator;
import Tools.Extended.SelectorExt;
import Tools.Extended.ServerSocketChannelExt;
import Tools.UserManagement.ClientQueueManager;
import Tools.UserManagement.Register;

import java.nio.channels.SelectionKey;
import java.util.Iterator;

public abstract class TemplateServer {
    public ServerSocketChannelExt serverSocketChannel;
    public SelectorExt selector;

    public Register register;
    public ClientQueueManager clientQueues;
    public IOCommunicator communicator;

    public TemplateServer(int port) {
        serverSocketChannel = new ServerSocketChannelExt();
        serverSocketChannel.bind(port);

        selector = new SelectorExt();
        register = new Register();
        clientQueues = new ClientQueueManager();
        communicator = new IOCommunicator();
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


    public void close() {
        serverSocketChannel.close();
        selector.close();
    }


    protected abstract void acceptKey(SelectionKey key);

    protected abstract void readKey(SelectionKey key);

    protected abstract void writeKey(SelectionKey key);
}
