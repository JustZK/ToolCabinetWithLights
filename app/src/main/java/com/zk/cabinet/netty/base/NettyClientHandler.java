package com.zk.cabinet.netty.base;

import com.zk.cabinet.util.LogUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final String TAG = "NettyClientHandler";
    private NettyClientEventProcessor nettyClientEventProcessor;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (nettyClientEventProcessor != null) nettyClientEventProcessor.onMessageReceived(byteBuf.array());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        LogUtil.getInstance().d(TAG, "userEventTriggered", false);
        super.userEventTriggered(ctx, evt);
        if (nettyClientEventProcessor != null && evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    LogUtil.getInstance().d(TAG, "WRITER_IDLE", false);
                    nettyClientEventProcessor.onWriteIdle();
                    break;
                case READER_IDLE:
                    nettyClientEventProcessor.onReadIdle();
                    break;
                case ALL_IDLE:
                    nettyClientEventProcessor.onAllIdle();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.getInstance().d(TAG, "channelActive", false);
        super.channelActive(ctx);
        if (nettyClientEventProcessor != null) nettyClientEventProcessor.onChannelActive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.getInstance().d(TAG, "exceptionCaught:" + cause.getMessage(), false);
        super.exceptionCaught(ctx, cause);
        // 异常则关闭
        ctx.close();
        if (nettyClientEventProcessor != null) nettyClientEventProcessor.onExceptionCaught();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.getInstance().d(TAG, "channelInactive", false);
        super.channelInactive(ctx);
        if (nettyClientEventProcessor != null) nettyClientEventProcessor.onChannelInactive();
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        LogUtil.getInstance().d(TAG, "disconnect", false);
        super.disconnect(ctx, promise);
        if (nettyClientEventProcessor != null) nettyClientEventProcessor.onDisconnect();
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        LogUtil.getInstance().d(TAG, "close", false);
        super.close(ctx, promise);
        if (nettyClientEventProcessor != null) nettyClientEventProcessor.onClose();
    }

    public NettyClientEventProcessor getNettyClientEventProcessor() {
        return nettyClientEventProcessor;
    }

    public void setNettyClientEventProcessor(NettyClientEventProcessor nettyClientEventProcessor) {
        this.nettyClientEventProcessor = nettyClientEventProcessor;
    }


    public static abstract class NettyClientEventProcessor {

        protected void onWriteIdle () {}

        protected void onReadIdle () {}

        protected void onAllIdle () {}

        protected void onChannelActive () {}

        protected void onExceptionCaught () {}

        protected void onChannelInactive () {}

        protected void onDisconnect () {}

        protected void onClose () {}

        protected abstract void onMessageReceived(byte[] buffer);

    }
}
