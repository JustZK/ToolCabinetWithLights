package com.zk.cabinet.netty.base;

import com.zk.cabinet.util.LogUtil;

import java.net.InetSocketAddress;
import java.util.LinkedList;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by ZK on 2018/2/5.
 */

public class NettyClientBootstrap {

    private static final String TAG = "NettyClientBootstrap";

    private int port;
    private String host;
    private Bootstrap bootstrap;

    private NioEventLoopGroup eventLoopGroup;
    private SocketChannel socketChannel;
    private SimpleChannelInboundHandler<Object> nettyClientHandler;

    // 写线程控制变量
    private boolean ctrlWrite;
    // 写线程锁
    private final Object syncWrite;

    // 心跳
    private boolean heartBeat;

    // 待发送数据队列
    private LinkedList<byte[]> dataList;

    public NettyClientBootstrap(String host, int port, final SimpleChannelInboundHandler<Object> nettyClientHandler) {
        this.port = port;
        this.host = host;

        this.nettyClientHandler = nettyClientHandler;

        heartBeat = false;
        ctrlWrite = false;
        syncWrite = new Object();
        dataList = new LinkedList<>();

        //初始化连接

        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(1024, 1024 * 32, 1024 * 64));
        bootstrap.group(eventLoopGroup);
        bootstrap.remoteAddress(host, port);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleStateHandler(60, 30, 0));
                socketChannel.pipeline().addLast(new ByteArrayEncoder());
                socketChannel.pipeline().addLast(nettyClientHandler);
            }
        });
    }


    private ChannelFuture future = null;
    public void connect () {
        try {
            future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            if (future.isSuccess()) {
                LogUtil.getInstance().d(TAG, "connect:bootstrap connect success", false);
                socketChannel = (SocketChannel) future.channel();
                if (socketChannel != null) {
                    ctrlWrite = true;
                    sendThread = new Thread(sendRunnable);
                    sendThread.start();
                } else {
                    LogUtil.getInstance().d(TAG, "connect:socketChannel == null", false);
                }

            } else {
                LogUtil.getInstance().d(TAG, "connect:bootstrap connect fail", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect () {
        future.channel().disconnect();
    }

    public void close () {
        ctrlWrite = false;
        try {
            future.channel().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Thread sendThread;
    private final Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            {
                byte[] dataOperating = null;
                while (ctrlWrite) {
                    synchronized (syncWrite) {
                        if (dataList.size() == 0) {
                            try {
                                syncWrite.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        LogUtil.getInstance().d("syncWrite.wait");
                    }

                    try {
                        dataOperating = dataList.poll();
                    } catch (Exception e) {
                        e.printStackTrace();
                     }

                    if (dataOperating != null) {

                        StringBuilder buffers = new StringBuilder();
                        for (int i = 0; i < dataOperating.length ; i++) {
                            buffers.append(Integer.toHexString((dataOperating[i] & 0xff)));
                            buffers.append(" ");
                        }
                        LogUtil.getInstance().d("test sendRunnable  writeAndFlush:::", buffers.toString());

                        // 写数据
                        socketChannel.writeAndFlush(dataOperating);
                    }

                }
            }
        }
    };

    public void send (final byte[] msg) {

        synchronized (syncWrite) {
            if (msg != null) dataList.offer(msg);
            syncWrite.notify();
        }

    }

}
