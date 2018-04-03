package top.evodb;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.network.Reactor;


/**
 * @author ynfeng
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String args[]) {
        Reactor reactor;
        try {
            reactor = Reactor.getInstance();
            reactor.start();
        } catch (IOException e) {
            LOGGER.error("Start server error.", e);
        }
    }
}
