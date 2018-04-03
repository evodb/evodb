package top.evodb.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author evodb
 */
public final class Acceptor extends Thread {
    private Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private static volatile Acceptor instance;
    private static final int SELECT_TIMEOUT = 500;
    private final Reactor reactor;
    private static final Logger LOGGER = LoggerFactory.getLogger(Acceptor.class);

    public static Acceptor getInstance(String bindIp, int bindPort, Reactor reactor) throws IOException {
        if (instance == null) {
            synchronized (Acceptor.class) {
                if (instance == null) {
                    instance = new Acceptor(bindIp, bindPort, reactor);
                }
            }
        }
        return instance;
    }

    @Override
    public synchronized void start() {
        super.start();
        LOGGER.info(getName() + " start.");
    }

    private Acceptor(String bindIp, int bindPort, Reactor reactor) throws IOException {
        selector = Selector.open();
        this.reactor = reactor;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(bindIp, bindPort));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        setName("acceptor-thread");
    }

    private void shutdown() {
        interrupt();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            int numOfReadyKey = 0;
            try {
                numOfReadyKey = selector.select(SELECT_TIMEOUT);
            } catch (IOException e) {
                LOGGER.warn(getName() + " select error.", e);
            }
            if (numOfReadyKey > 0) {
                Set<SelectionKey> selectKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey selectionKey = it.next();
                    if (selectionKey.isAcceptable()) {
                        try {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            LOGGER.info(getName() + " " + socketChannel.getRemoteAddress() + " connected.");

                        } catch (IOException e) {
                            LOGGER.warn(getName() + " accpet error.", e);
                        }
                    }
                }
            }
        }
        LOGGER.info(getName() + " stop.");
    }
}
