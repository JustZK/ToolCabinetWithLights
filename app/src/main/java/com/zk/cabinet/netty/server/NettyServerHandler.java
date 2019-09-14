package com.zk.cabinet.netty.server;

import com.zk.cabinet.util.LogUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final String TAG = "b";
    private NettyServerHandler.NettyServerEventProcessor nettyServerEventProcessor;

//    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        Channel channel = ctx.channel();
        System.out.println("Client ："+channel.remoteAddress()+"  在线\n");
        if (nettyServerEventProcessor != null) nettyServerEventProcessor.onChannelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        Channel channel = ctx.channel();
        System.out.println("Client ："+channel.remoteAddress()+"  离线\n");
        if (nettyServerEventProcessor != null) nettyServerEventProcessor.onChannelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // TODO Auto-generated method stub
//        Channel channel = ctx.channel();
//        System.out.println("Client ："+channel.remoteAddress()+"  异常\n");
//        cause.printStackTrace();
//        ctx.close();
//        if (nettyServerEventProcessor != null) nettyServerEventProcessor.onExceptionCaught(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        Channel channel = ctx.channel();
        System.out.println("-------handlerAdded");
        if (nettyServerEventProcessor != null) nettyServerEventProcessor.onHandlerAdded(ctx);
//        channels.writeAndFlush("[c]: "+channel.remoteAddress()+" 加入\n");
//        channels.add(channel);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        Channel channel = ctx.channel();
        System.out.println("-------handlerRemoved");
        if (nettyServerEventProcessor != null) nettyServerEventProcessor.onHandlerRemoved(ctx);
//        channels.writeAndFlush("[c]: "+channel.remoteAddress()+" 离开\n");
//        // A closed Channel is automatically removed from ChannelGroup,
//        // so there is no need to do "channels.remove(ctx.channel());"
//        channels.remove(channel);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf byteBuf = (ByteBuf) msg;

        if (nettyServerEventProcessor != null) nettyServerEventProcessor.onMessageReceived(ctx, byteBuf.array());
    }

    @Override
        protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        LogUtil.getInstance().d(TAG, "---channelRead3 hava received");
    }

    public NettyServerHandler.NettyServerEventProcessor getNettyServerEventProcessor() {
        return nettyServerEventProcessor;
    }

    public void setNettyServerEventProcessor(NettyServerHandler.NettyServerEventProcessor nettyServerEventProcessor) {
        this.nettyServerEventProcessor = nettyServerEventProcessor;
    }

    public static abstract class NettyServerEventProcessor {

        protected void onChannelActive (ChannelHandlerContext ctx) {}

        protected void onChannelInactive (ChannelHandlerContext ctx) {}

        protected void onExceptionCaught (ChannelHandlerContext ctx) {}

        protected void onHandlerAdded (ChannelHandlerContext ctx) {}

        protected void onHandlerRemoved (ChannelHandlerContext ctx) {}

        protected abstract void onMessageReceived(ChannelHandlerContext ctx, byte[] buffer);

    }
}
