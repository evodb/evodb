package top.evodb.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/**
 * @author evodb
 */
public final class Reactor {

    private static final String REACTOR_THREAD_NAME_PREFIX = "R_THREAD";
    private final int numOfReactorThreads;
    private static final int SELECT_TIMEOUT = 500;
    private int currentReactorThread;
    private final ReactorThread[] reactorThreads;
    private static final Logger LOGGER = LoggerFactory.getLogger(Reactor.class);
    private static volatile Reactor instance;

    public static Reactor getInstance() throws IOException {
        if (instance == null) {
            synchronized (Reactor.class) {
                if (instance == null) {
                    instance = new Reactor();
                }
            }
        }
        return instance;
    }

    private Reactor() throws IOException {
        currentReactorThread = 0;
        numOfReactorThreads = Runtime.getRuntime().availableProcessors();
        reactorThreads = new ReactorThread[numOfReactorThreads];
        for (int i = 0; i < reactorThreads.length; i++) {
            reactorThreads[i] = new ReactorThread(REACTOR_THREAD_NAME_PREFIX + i);
        }
    }

    public void start() {
        for (ReactorThread reactorThread : reactorThreads) {
            reactorThread.start();
        }
        LOGGER.info("All reactor thread have stared.");
    }

    public void register(SocketChannel socketChannel) {
        ReactorThread reactorThread = nextReactorThread();
        reactorThread.register(socketChannel);
        try {
            LOGGER.debug(
                "Register a channel[" + socketChannel.getRemoteAddress() + "] on " + reactorThread
                    .getName());
        } catch (IOException e) {
            LOGGER.warn("Get socket channel remote address error.", e);
        }
    }

    private ReactorThread nextReactorThread() {
        int nextRectorThread = currentReactorThread % numOfReactorThreads;
        currentReactorThread++;
        return reactorThreads[nextRectorThread];
    }

    public void shutdown() {
        for (int i = 0; i < reactorThreads.length; i++) {
            reactorThreads[i].interrupt();
        }
    }


    private class ReactorThread extends Thread {

        private Selector selector;
        private LinkedList<SocketChannel> registerQueue = new LinkedList<>();

        private ReactorThread(String name) throws IOException {
            selector = Selector.open();
            setName(name);
            LOGGER.debug("Create reactor thread[" + name + "]");
        }

        public void register(SocketChannel socketChannel) {
            registerQueue.push(socketChannel);
            selector.wakeup();
        }

        private void doRegister() throws ClosedChannelException {
            for (; ; ) {
                SocketChannel socketChannel = registerQueue.poll();
                socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                socketChannel = registerQueue.poll();
                if (socketChannel == null) {
                    break;
                }
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                int numOfReadKey = 0;
                try {
                    numOfReadKey = selector.select(SELECT_TIMEOUT);
                } catch (IOException e) {
                    LOGGER.warn(getName() + " select error.", e);
                }
                if (numOfReadKey > 0) {

                }
                if (!registerQueue.isEmpty()) {
                    try {
                        doRegister();
                    } catch (ClosedChannelException e) {
                        LOGGER.warn(getName() + " register error.", e);
                    }
                }
            }
            LOGGER.info(getName() + " shoutdown.");
        }
    }

}
