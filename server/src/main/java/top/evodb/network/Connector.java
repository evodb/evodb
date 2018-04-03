package top.evodb.network;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * @author evodb
 */
public class Connector extends Thread {
    private Selector selector;

    private Connector() throws IOException {
        selector = Selector.open();
    }

    @Override
    public void run() {
        super.run();
    }


}
