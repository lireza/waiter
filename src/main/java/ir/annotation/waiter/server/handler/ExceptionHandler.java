package ir.annotation.waiter.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ir.annotation.waiter.server.Error;
import org.msgpack.core.MessagePack;
import org.msgpack.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelHandler.Sharable;
import static ir.annotation.waiter.server.util.MessagePackUtil.*;

/**
 * Channel inbound exception handler that is inserted as last channel handler in channel pipeline to handle all kinds of {@link Throwable}.
 * <p>
 * This sharable class is a general exception handler that logs the exception, sends back appropriate response and finally closes the channel.
 * </p>
 *
 * @author Alireza Pourtaghi
 */
@Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    /**
     * Appropriate binary message for unknown error.
     */
    private final Value unknownErrorMessage;

    /**
     * Constructor to instantiate and setup exception handler.
     */
    public ExceptionHandler() {
        unknownErrorMessage = map(
                string("succ"), bool(false),
                string("errs"), array(map(
                        string("code"), string(Error.Reason.UNKNOWN.getError().getCode()),
                        string("mess"), string(Error.Reason.UNKNOWN.getError().getMessage())
                ))
        );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            logger.error("exception caught ", cause);

            try (var buffer = MessagePack.newDefaultBufferPacker()) {
                buffer.packValue(unknownErrorMessage);
                var bytesOut = ctx.alloc().buffer((int) buffer.getTotalWrittenBytes()); // Default to allocate direct buffer.
                bytesOut.writeBytes(buffer.toByteArray());
                ctx.writeAndFlush(bytesOut);
            }
        } finally {
            ctx.close();
        }
    }
}
