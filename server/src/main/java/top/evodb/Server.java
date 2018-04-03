package top.evodb;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.network.Acceptor;
import top.evodb.network.Reactor;


/**
 * @author evodb
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String args[]) {
        Reactor reactor;
        Acceptor acceptor;
        try {
            reactor = Reactor.getInstance();
            acceptor = Acceptor.getInstance("127.0.0.1", 9600, reactor);
            acceptor.start();
        } catch (IOException e) {
            LOGGER.error("Start server error.", e);
        }
    }
}
