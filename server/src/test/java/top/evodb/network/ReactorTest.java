package top.evodb.network;

import org.junit.Test;
import top.evodb.exception.ReactorPoolException;

/**
 * @author evodb
 */
public class ReactorTest {

    @Test
    public void restReactorStart() throws ReactorPoolException {
        Reactor reactor = Reactor.getInstance();
        reactor.start();
    }

    @Test
    public void restReactorShutdown() throws ReactorPoolException, InterruptedException {
        Reactor reactor = Reactor.getInstance();
        reactor.start();
        reactor.shutdown();
        Thread.sleep(1000);
    }
}
