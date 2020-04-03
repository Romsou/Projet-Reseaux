package Tools.Extended;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class SocketChannelExt {
    public SocketChannel socketChannel;

    public void setSocketChannel(SocketChannel channel) {
        socketChannel = channel;
    }

    public void configureBlocking(boolean flag) {
        try {
            socketChannel.configureBlocking(flag);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ErrorCodes.CONFIG_FAIL.getCode());
        }
    }

    public void register(SelectorExt selector, int selectionKeyMask) {
        try {
            socketChannel.register(selector.selector, selectionKeyMask);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            System.exit(ErrorCodes.REGISTER_FAIL.getCode());
        }
    }

    public void getServerFromKey(SelectionKey key) {
        socketChannel = (SocketChannel) key.channel();
    }

    @Override
    public int hashCode() {
        return socketChannel.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocketChannelExt that = (SocketChannelExt) o;
        return socketChannel.equals(that.socketChannel);
    }
}
