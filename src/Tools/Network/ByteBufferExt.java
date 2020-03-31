package Tools.Network;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteBufferExt {
    private ByteBuffer buffer;


    public ByteBufferExt() {
        this.buffer = ByteBuffer.allocate(1028);
    }


    public ByteBufferExt(int size) {
        this.buffer = ByteBuffer.allocate(size);
    }

    /**
     * Cleans the buffer to avoid problems
     */
    public void cleanBuffer() {
        buffer.clear();
        buffer.put(new byte[1028]);
        buffer.clear();
    }


    /**
     * Convert buffer's content into a String for further processing
     *
     * @return A string representing the content of the buffer
     */
    public String convertBufferToString() {
        return new String(buffer.array(), StandardCharsets.UTF_8).trim();
    }


    public void put(byte[] bytes) {
        buffer.put(bytes);
    }


    public void flip() {
        buffer.flip();
    }


    public ByteBuffer getBuffer() {
        return buffer;
    }


    public void clear() {
        buffer.clear();
    }

    public byte[] array() {
        return buffer.array();
    }

}
