package discard

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.nio.charset.StandardCharsets
import kotlin.test.Test

private class DiscardClientHandler : ChannelInboundHandlerAdapter() {
    override fun channelRead(
        context: ChannelHandlerContext,
        message: kotlin.Any,
    ) {
        return
    }
}

private class DiscardClientSocketInitialiser :
    ChannelInitializer<SocketChannel>() {
    override fun initChannel(channel: SocketChannel) {
        channel.pipeline()
            .addLast(
                DiscardClientHandler(),
            )

        return
    }
}

class DiscardClient {
    val eventLoopGroup: EventLoopGroup = NioEventLoopGroup()

    val bootstrap = Bootstrap()

    lateinit var future: ChannelFuture

    init {
        val handler = DiscardClientSocketInitialiser()

        bootstrap.group(eventLoopGroup)

        bootstrap.channel(NioSocketChannel::class.java)

        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)

        bootstrap.handler(handler)
    }

    fun connect(serverHost: String, serverPort: Int) {
        future = bootstrap.connect(serverHost, serverPort)
            .sync()

        return
    }

    fun send(message: String) {
        val bytes: ByteBuf = Unpooled.copiedBuffer(
            message,
            StandardCharsets.UTF_8,
        )

        future.channel().writeAndFlush(bytes)

        return
    }

    fun shutdown() {
        eventLoopGroup.shutdownGracefully()

        return
    }
}

class DiscardServerTest {
    @Test fun discardServerIsUp() {
        /*
         * A Discard server is up if it accepts connections and receives data.
         */

        val client = DiscardClient()

        client.connect("localhost", 2509)

        client.send("HELO server\n")

        client.shutdown()

        return
    }
}
