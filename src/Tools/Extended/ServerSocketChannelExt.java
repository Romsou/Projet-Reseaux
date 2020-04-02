package Tools.Extended;

import java.io.IOException;
import java.net.InetSocketAddress;
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

    public void close() {
        try {
            this.serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ErrorCodes.CLOSING_FAIL.getCode());
        }
    }


}

enum ErrorCodes {
    OPEN_FAIL(10),
    CONFIG_FAIL(20),
    ACCEPT_FAIL(30),
    CLOSING_FAIL(40);

    private int code;

    ErrorCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

