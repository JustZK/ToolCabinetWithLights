package com.zk.cabinet.netty.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class SimpleInitializer  extends ChannelInitializer<SocketChannel> {
    private NettyServerParsingLibrary.Processor processor;
//    NettyServerHandler nettyServerHandler;

    public SimpleInitializer(NettyServerParsingLibrary.Processor processor){
        this.processor = processor;


    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        NettyServerHandler nettyServerHandler = new NettyServerHandler();
        nettyServerHandler.setNettyServerEventProcessor(processor);
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(60, 30, 0))
                .addLast(new ByteArrayEncoder())
                .addLast("handler",nettyServerHandler);
    }
}
