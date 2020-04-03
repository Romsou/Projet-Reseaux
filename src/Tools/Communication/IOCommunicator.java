package Tools.Communication;

import Tools.Extended.ByteBufferExt;
import Tools.Extended.ErrorCodes;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class IOCommunicator {
    private ByteBufferExt buffer;

    public boolean hasReceived(SocketChannel client) {
        return buffer.read(client) > 0;
    }

    public String receive(SocketChannel client) {
        return buffer.convertBufferToString();
    }

    public void send(SocketChannel client, String message) {
        buffer.cleanBuffer();
        buffer.put(message.getBytes());
        buffer.flip();
        try {
            client.write(buffer.getBuffer());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ErrorCodes.SENDING_FAIL.getCode());
        }
    }
}
