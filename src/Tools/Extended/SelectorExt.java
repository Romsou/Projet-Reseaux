package Tools.Extended;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class SelectorExt {
    public Selector selector;


    public SelectorExt() {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ErrorCodes.OPEN_FAIL.getCode());
        }
    }


    public int select() {
        try {
            return selector.select();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(ErrorCodes.SELECTION_FAIL.getCode());
            return 0;
        }
    }


    public Iterator<SelectionKey> getSelectedKeysIterator() {
        return selector.selectedKeys().iterator();
    }


    public void close() {
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ErrorCodes.CLOSING_FAIL.getCode());
        }
    }


}
