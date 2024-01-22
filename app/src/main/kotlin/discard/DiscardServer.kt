package discard

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

private class DiscardServerHandler : ChannelInboundHandlerAdapter() {
    override fun channelRead(
        context: ChannelHandlerContext,
        message: kotlin.Any,
    ) {
        val bytes = message as ByteBuf

        // discard the received data
        bytes.release()

        return
    }

    override fun exceptionCaught(
        context: ChannelHandlerContext,
        captive: Throwable,
    ) {
        captive.printStackTrace()

        // close the connection
        context.close()

        return
    }
}

private class DiscardServerSocketInitialiser :
    ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        channel.pipeline()
            .addLast(
                DiscardServerHandler(),
            )

        return
    }
}

class DiscardServer {
    companion object {
        const val BACKLOG = 128 // XXX: arbitrary
    }

    val acceptorEventLoopGroup: EventLoopGroup = NioEventLoopGroup()
    val receiverEventLoopGroup: EventLoopGroup = NioEventLoopGroup()

    val bootstrap = ServerBootstrap()

    init {
        val handler = DiscardServerSocketInitialiser()

        bootstrap.group(acceptorEventLoopGroup, receiverEventLoopGroup)

        bootstrap.channel(NioServerSocketChannel::class.java)

        bootstrap.option(ChannelOption.SO_BACKLOG, BACKLOG)

        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)

        bootstrap.childHandler(handler)
    }

    fun listenAndServe(host: String, port: Int) {
        bootstrap.bind(host, port)
            .sync()

        return
    }

    fun shutdown() {
        acceptorEventLoopGroup.shutdownGracefully()
        receiverEventLoopGroup.shutdownGracefully()

        return
    }
}
