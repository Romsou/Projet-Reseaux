package Tools.Extended;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerSocketChannelExt {
    protected ServerSocketChannel serverChannel;


    public ServerSocketChannelExt() {
        try {
            serverChannel = ServerSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ErrorCodes.OPEN_FAIL.getCode());
        }
    }


    public void bind(int port) {
        InetSocketAddress localAddress = new InetSocketAddress(port);
        try {
            serverChannel.bind(localAddress);
            serverChannel.configureBlocking(false);
            serverChannel.socket().setReuseAddress(true);
        } catch (IOException e) {
            System.out.println(localAddress);
            e.printStackTrace();
            System.exit(ErrorCodes.CONFIG_FAIL.getCode());
        }
    }


    public SocketChannel accept() {
        try {
            return serverChannel.accept();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ErrorCodes.ACCEPT_FAIL.getCode());
            return null;
        }
    }


    public void getServerFromKey(SelectionKey key) {
        serverChannel = (ServerSocketChannel) key.channel();
    }


    public void register(SelectorExt selector, int selectionKeyMask) {
        try {
            serverChannel.register(selector.selector, selectionKeyMask);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            System.exit(ErrorCodes.REGISTER_FAIL.getCode());
        }
    }


    public void close() {
        try {
            this.serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ErrorCodes.CLOSING_FAIL.getCode());
        }
    }

}
