package Serveur.Federation;

import Serveur.AbstractServers.AbstractSelectorServer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SlaveServer extends AbstractSelectorServer {
    HashMap<SocketChannel, ConcurrentLinkedDeque> clientQueue;
    private SocketChannel masterServer;


    public SlaveServer(int port) {
        super(port);
        this.clientQueue = new HashMap<>();
    }

    @Override
    protected void writeMessageToClients(String message) {

    }

    @Override
    protected void treatReadable(SelectionKey key) throws IOException {

    }

    @Override
    protected void treatAcceptable(SelectionKey key) throws IOException {

    }

    @Override
    protected void treatWritable(SelectionKey key) throws IOException {

    }


}
