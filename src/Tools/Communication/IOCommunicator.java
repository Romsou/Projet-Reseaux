package Tools.Communication;

import Tools.Extended.ByteBufferExt;
import Tools.Extended.ErrorCodes;
import Tools.Extended.SocketChannelExt;

import java.io.IOException;

public class IOCommunicator {
    private ByteBufferExt buffer;

    public IOCommunicator() {
        buffer = new ByteBufferExt();
    }

    public boolean hasReceived(SocketChannelExt client) {
        return buffer.read(client.socketChannel) > 0;
    }

    public String receive() {
        return buffer.convertBufferToString();
    }

    public int send(SocketChannelExt client, String message) {
        buffer.cleanBuffer();
        buffer.put(message.getBytes());
        buffer.flip();
        try {
            client.socketChannel.write(buffer.getBuffer());
        } catch (IOException e) {
            System.out.println("Remote connection lost");
            return ErrorCodes.SENDING_FAIL.getCode();
        }
        return 0;
    }
}
