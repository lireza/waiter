package ir.annotation.waiter.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ir.annotation.waiter.server.Error;
import org.msgpack.core.MessagePack;
import org.msgpack.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelHandler.Sharable;
import static ir.annotation.waiter.utils.MessagePackUtil.*;

/**
 * Channel inbound exception handler that is inserted as last channel handler in channel pipeline to handle all kinds of {@link Throwable}.
 * <p>
 * This sharable class is a general exception handler that logs the exception, sends back appropriate response and finally closes the channel.
 * </p>
 *
 * @author Alireza Pourtaghi
 */
@Sharable
public class ChannelInboundExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ChannelInboundExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            logger.error("exception caught ", cause);

            try (var buffer = MessagePack.newDefaultBufferPacker()) {
                buffer.packValue(buildInternalServerErrorMessage());
                var bytesOut = ctx.alloc().directBuffer((int) buffer.getTotalWrittenBytes());
                bytesOut.writeBytes(buffer.toByteArray());
                ctx.writeAndFlush(bytesOut);
            }
        } finally {
            ctx.close();
        }
    }

    /**
     * Generates appropriate binary message.
     *
     * @return Message pack's {@link Value} format.
     */
    private Value buildInternalServerErrorMessage() {
        return map(
                string("successful"), bool(false),
                string("errors"), array(map(
                        string("code"), string(Error.Reason.INTERNAL_SERVER_ERROR.getError().getCode()),
                        string("message"), string(Error.Reason.INTERNAL_SERVER_ERROR.getError().getMessage())
                ))
        );
    }
}
