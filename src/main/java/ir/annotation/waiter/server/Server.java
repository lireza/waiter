package ir.annotation.waiter.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import ir.annotation.waiter.core.application.Component;
import ir.annotation.waiter.server.util.OSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static ir.annotation.waiter.server.util.OSUtil.OS.*;

/**
 * A netty based server socket implementation.
 *
 * @author Alireza Pourtaghi
 */
public final class Server extends Component<Server> {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    /**
     * Event loop group that must be assigned to netty server for handling IO operations.
     */
    private EventLoopGroup eventLoopGroup;

    /**
     * Host value that this server must listen on.
     */
    private String host;

    /**
     * Port number that this server must listen on.
     */
    private int port;

    /**
     * Maximum amount of frame size in kilo bytes.
     */
    private int maxFrameSize;

    /**
     * Public accessible constructor to identify this component.
     */
    public Server() {
        super("server");
    }

    /**
     * Private constructor to build an instance of this server implementation.
     *
     * @param eventLoopGroup Event loop group that must be assigned to netty server for handling IO operations.
     * @param host           Host value that this server must listen on.
     * @param port           Port number that this server must listen on.
     * @param maxFrameSize   Maximum amount of frame size in kilo bytes.
     */
    private Server(EventLoopGroup eventLoopGroup, String host, int port, int maxFrameSize) {
        this();
        this.eventLoopGroup = eventLoopGroup;
        this.host = host;
        this.port = port;
        this.maxFrameSize = maxFrameSize;
    }

    @Override
    public Server setup(Properties properties) {
        var os = OSUtil.detectOS();

        var eventLoopGroup = os.equals(LINUX) ? new EpollEventLoopGroup() : os.equals(OSX) || os.equals(BSD) ? new KQueueEventLoopGroup() : new NioEventLoopGroup();
        var host = properties.getOrDefault("server.host", "0.0.0.0").toString();
        var port = Integer.parseInt(properties.getOrDefault("server.port", "9000").toString());
        var maxFrameSize = Integer.parseInt(properties.getOrDefault("server.max-frame-size", "1024").toString());

        return new Server(eventLoopGroup, host, port, maxFrameSize);
    }

    @Override
    public void start() throws InterruptedException {
        logger.info("starting server on {}:{} ...", getHost(), getPort());
        var serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(getEventLoopGroup());
        if (getEventLoopGroup() instanceof NioEventLoopGroup) {
            serverBootstrap.channel(NioServerSocketChannel.class);
        } else if (getEventLoopGroup() instanceof EpollEventLoopGroup) {
            logger.info("using epoll native transport");
            serverBootstrap.channel(EpollServerSocketChannel.class);
        } else if (getEventLoopGroup() instanceof KQueueEventLoopGroup) {
            logger.info("using kqueue native transport");
            serverBootstrap.channel(KQueueServerSocketChannel.class);
        }
        serverBootstrap.localAddress(getHost(), getPort());
        serverBootstrap.childHandler(new Initializer(getMaxFrameSize()));

        serverBootstrap.bind().sync();
    }

    @Override
    public void stop() throws InterruptedException {
        logger.info("stopping server ...");
        eventLoopGroup.shutdownGracefully().sync();
    }

    private EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    private String getHost() {
        return host;
    }

    private int getPort() {
        return port;
    }

    private int getMaxFrameSize() {
        return maxFrameSize;
    }
}
