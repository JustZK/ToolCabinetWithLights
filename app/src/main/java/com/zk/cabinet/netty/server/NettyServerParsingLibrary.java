package com.zk.cabinet.netty.server;

import com.zk.cabinet.bean.InventoryInfo;
import com.zk.cabinet.bean.NettySendInfo;
import com.zk.cabinet.callback.InventoryListener;
import com.zk.cabinet.netty.base.NettyGroupPackage;
import com.zk.cabinet.netty.base.NettyUtils;
import com.zk.cabinet.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServerParsingLibrary {
    private volatile static NettyServerParsingLibrary instance;
    public static final String TAG = "NettyParsingLibrary";
    private int port = 8080;

    public NettyServerParsingLibrary.Processor processor = null;

    private NettyServerParsingLibrary() {
    }

    public static NettyServerParsingLibrary getInstance() {
        if (instance == null) {
            synchronized (NettyServerParsingLibrary.class) {
                if (instance == null)
                    instance = new NettyServerParsingLibrary();
            }
        }
        return instance;
    }

    public void init(int port) {
        this.port = port;
        processor = new NettyServerParsingLibrary.Processor();

    }

    public void start() {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(1024, 1024 * 32, 1024 * 64))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new SimpleInitializer(processor));

            ChannelFuture future = bootstrap.bind(this.port).sync();
            System.out.println("服务器已经启动");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public boolean send(NettySendInfo nettySendInfo) {
        return processor.send(nettySendInfo);
    }

    public boolean isOnline(int readerIp) {
        return processor.isOnline(readerIp);
    }

    public static class Processor extends NettyServerHandler.NettyServerEventProcessor {
        private HashMap<Integer, ChannelHandlerContext> nettyChannelMap = new HashMap<>();
        private int readerID;
        private byte[] remainBuffer = null;//上次解析剩余的数据
        private List<InventoryInfo> inventoryInfoList = new ArrayList<>();
        private InventoryListener inventoryListener;

        public void onInventoryListener(InventoryListener inventoryListener) {
            this.inventoryListener = inventoryListener;
        }

        public boolean isOnline(int readerIp) {
            return nettyChannelMap.get(readerIp) != null;
        }

        public boolean send(NettySendInfo nettySendInfo) {
            readerID = nettySendInfo.getReaderIp();
            if (nettyChannelMap.get(nettySendInfo.getReaderIp()) != null) {
                switch (nettySendInfo.getCommunicationType()) {
                    case 0x03:
                        nettyChannelMap.get(nettySendInfo.getReaderIp()).channel().writeAndFlush(NettyGroupPackage.inventory(nettySendInfo.getFastID(),
                                nettySendInfo.getAntennaNumber(), nettySendInfo.getInventoryType()));


                        byte[] buffer = NettyGroupPackage.inventory(nettySendInfo.getFastID(),
                                nettySendInfo.getAntennaNumber(), nettySendInfo.getInventoryType());
                        StringBuilder buffers = new StringBuilder();
                        for (int i = 0; i < buffer.length; i++) {
                            buffers.append(Integer.toHexString((buffer[i] & 0xff)));
                            buffers.append(" ");
                        }
                        LogUtil.getInstance().d("NETTY通信Android发送盘点指令：" + buffers);
                        break;
                }
                return true;
            } else {
                if (inventoryListener != null)
                    inventoryListener.inventoryList(readerID, 1, inventoryInfoList);
                LogUtil.getInstance().LogPrint("nettyChannelMap.get(nettySendInfo.getReaderIp()) != null");
                return false;
            }
        }

        @Override
        protected void onMessageReceived(ChannelHandlerContext ctx, byte[] buffer) {
            LogUtil.getInstance().d(TAG, "onMessageReceived");
            StringBuilder buffers = new StringBuilder();
            for (int i = 0; i < buffer.length; i++) {
                buffers.append(Integer.toHexString((buffer[i] & 0xff)));
                buffers.append(" ");
            }
            LogUtil.getInstance().d("test Received" + buffers);
            LogUtil.getInstance().LogPrint("NETTY通信Android收到原始数据：" + buffers);

            byte[] tempBytes;
            //如果上次解析有剩余，则将其加上
            if (remainBuffer != null && remainBuffer.length != 0) {
                tempBytes = NettyUtils.bytesMerger(remainBuffer, buffer);
            } else {
                tempBytes = buffer;
            }

            remainBuffer = interceptionReceivedData(ctx, tempBytes);
        }

        @Override
        protected void onChannelActive(ChannelHandlerContext ctx) {
            super.onChannelActive(ctx);
        }

        @Override
        protected void onChannelInactive(ChannelHandlerContext ctx) {
            super.onChannelInactive(ctx);
            // 离线
            if (nettyChannelMap.containsValue(ctx)) {
                for (Map.Entry<Integer, ChannelHandlerContext> entry : nettyChannelMap.entrySet()) {
                    if (ctx.equals(entry.getValue())) {
                        LogUtil.getInstance().d("nettyChannelMap.remove(entry.getKey()) :" + entry.getKey());
                        nettyChannelMap.remove(entry.getKey());
                        if (inventoryListener != null)
                            inventoryListener.inventoryList(entry.getKey(), 1, inventoryInfoList);
                        break;
                    }
                }
            }
        }

        @Override
        protected void onExceptionCaught(ChannelHandlerContext ctx) {
            super.onExceptionCaught(ctx);
            // 异常
            if (nettyChannelMap.containsValue(ctx)) {
                for (Map.Entry<Integer, ChannelHandlerContext> entry : nettyChannelMap.entrySet()) {
                    if (ctx.equals(entry.getValue())) {
                        LogUtil.getInstance().d("nettyChannelMap.remove(entry.getKey()) :" + entry.getKey());
                        nettyChannelMap.remove(entry.getKey());
                        if (inventoryListener != null)
                            inventoryListener.inventoryList(entry.getKey(), 1, inventoryInfoList);
                        break;
                    }
                }
            }
        }

        @Override
        protected void onHandlerAdded(ChannelHandlerContext ctx) {
            super.onHandlerAdded(ctx);
        }

        @Override
        protected void onHandlerRemoved(ChannelHandlerContext ctx) {
            super.onHandlerRemoved(ctx);
            // 离开
            for (Map.Entry<Integer, ChannelHandlerContext> entry : nettyChannelMap.entrySet()) {
                if (ctx == entry.getValue()) {
                    LogUtil.getInstance().LogPrint("nettyChannelMap.remove(entry.getKey()) :" + entry.getKey());
                    nettyChannelMap.remove(entry.getKey());
                    if (inventoryListener != null)
                        inventoryListener.inventoryList(entry.getKey(), 1, inventoryInfoList);
                    break;
                }
            }
        }

        /**
         * 截取完整的帧
         *
         * @param dataBytes 加上上次剩余的帧后的数据
         * @return 返回截取剩剩余的帧
         */
        private byte[] interceptionReceivedData(ChannelHandlerContext ctx, byte[] dataBytes) {
            if (dataBytes == null || dataBytes.length == 0) {
                return null;
            }
            int size = dataBytes.length;
            //针头帧尾的位置
            int headPosition1 = -1, headPosition2 = -1, tailPosition1 = -1, tailPosition2 = -1;
            for (int i = 0; i < (size - 1); i++) {
                if (dataBytes[i] == NettyUtils.HEAD_HIGH && dataBytes[i + 1] == NettyUtils.HEAD_LOW) {
                    headPosition1 = i;
                    headPosition2 = i + 1;
                }
                if (dataBytes[i] == NettyUtils.TAIL_HIGH && dataBytes[i + 1] == NettyUtils.TAIL_LOW) {
                    tailPosition1 = i;
                    tailPosition2 = i + 1;
                    break;
                }
            }
            if (headPosition1 != -1) {
                if (headPosition2 != -1 && tailPosition1 != -1 && tailPosition2 != -1 && tailPosition1 > headPosition2) {
                    byte[] tempCompleteBytes = new byte[tailPosition2 - headPosition1 + 1];
                    System.arraycopy(dataBytes, headPosition1, tempCompleteBytes, 0, tailPosition2 - headPosition1 + 1);
                    checkReceived(ctx, tempCompleteBytes, tailPosition2 - headPosition1 + 1);
                    //如果一组数据中有多个帧，则将剩余的数据发送
                    if (size > (tailPosition2 + 1)) {
                        byte[] subTempBytes = new byte[size - tailPosition2 - 1];
                        System.arraycopy(dataBytes, tailPosition2 + 1, subTempBytes, 0, subTempBytes.length);
                        return interceptionReceivedData(ctx, subTempBytes);
                    } else {
                        return null;
                    }
                } else if (tailPosition2 == -1) {
                    byte[] subTempBytes = new byte[size - headPosition1];
                    System.arraycopy(dataBytes, headPosition1, subTempBytes, 0, subTempBytes.length);
                    return subTempBytes;
                }
            }
            return null;
        }

        /**
         * 帧校验
         *
         * @param buffer 一条完整的数据帧
         * @param size   改数据帧的长度（加上针头帧尾）
         */
        private void checkReceived(ChannelHandlerContext ctx, byte[] buffer, int size) {
            LogUtil.getInstance().d("checkReceived");
            StringBuilder buffers = new StringBuilder();
            for (int i = 0; i < size; i++) {
                buffers.append(Integer.toHexString((buffer[i] & 0xff)));
                buffers.append(" ");
            }
            LogUtil.getInstance().d("checkReceived", "test Received checkReceived :" + buffers);

            if (buffer[0] == NettyUtils.HEAD_HIGH && buffer[1] == NettyUtils.HEAD_LOW
                    && buffer[size - 2] == NettyUtils.TAIL_HIGH && buffer[size - 1] == NettyUtils.TAIL_LOW) {
                LogUtil.getInstance().d("1");
                //和校验
                if (NettyUtils.andCheck(buffer, size)) {
                    int T = NettyUtils.containCheck(buffer, size);
                    if (T >= 0) {
                        LogUtil.getInstance().d("1 -" + T);
                        byte[] tBuffer;
                        if (T > 0)
                            tBuffer = NettyUtils.translationForUnlock(buffer, size, T);
                        else
                            tBuffer = buffer;
                        LogUtil.getInstance().d("2");
                        //帧长度校验
                        if ((size - NettyUtils.HEAD_TAIL_NUMBER - T) == (tBuffer[2] & 0xff)) {
                            LogUtil.getInstance().d("3");
                            parser(ctx, tBuffer, size - T);
                        }

                    }
                }
            }
        }

        /**
         * 解析 分配
         *
         * @param buffer
         * @param size
         */
        private void parser(ChannelHandlerContext ctx, byte[] buffer, int size) {
            StringBuilder buffers = new StringBuilder();
            for (int i = 0; i < size; i++) {
                buffers.append(Integer.toHexString((buffer[i] & 0xff)));
                buffers.append(" ");
            }
            LogUtil.getInstance().d("test Received end" + buffers);
            LogUtil.getInstance().LogPrint("NETTY通信Android收到解析后的数据：" + buffers);


            switch (buffer[6]) {
                case 0x01: //注册
                    LogUtil.getInstance().d("注册");
                    ctx.channel().writeAndFlush(NettyGroupPackage.registered());
                    byte[] deviceIDB = new byte[4];
                    deviceIDB[0] = buffer[7];
                    deviceIDB[1] = buffer[8];
                    deviceIDB[2] = buffer[9];
                    deviceIDB[3] = buffer[10];
                    int deviceID = NettyUtils.byteArrayToInt(deviceIDB);
                    LogUtil.getInstance().d("设备编号：" + deviceID);
                    nettyChannelMap.put(deviceID, ctx);
                    LogUtil.getInstance().LogPrint("nettyChannelMap.put(deviceID, ctx); :" + deviceID);
                    break;
                case 0x04: //心跳
                    LogUtil.getInstance().d("心跳");
                    ctx.channel().writeAndFlush(NettyGroupPackage.keepLive());
                    break;
                case 0x08: //返回Inventory 结果

                    if (buffer[10] == 0x00) {
                        LogUtil.getInstance().d("Inventory start");
                        if (inventoryInfoList == null) inventoryInfoList = new ArrayList<>();
                        else inventoryInfoList.clear();
                    } else if (buffer[10] == 0x01) {
                        //盘点结束
                        LogUtil.getInstance().d("Inventory end");
                        if (inventoryListener != null) {
                            inventoryListener.inventoryList(readerID, 0, inventoryInfoList);
                        }
                    }
                    break;
                case 0x39: //Inventory 上报数据
                    LogUtil.getInstance().d("Inventory 上报数据");
                    String epcTempStr;
                    StringBuilder epcStr = new StringBuilder();
                    int epcLength = buffer[29] * 256 + buffer[30];
//                    char[] epcTempChar = new char[epcLength];
                    for (int i = 31; i < (31 + epcLength); i++) {
//                        epcTempChar[i - 31] = (char) buffer[i];

                        epcTempStr = Integer.toHexString(buffer[i] & 0xff);
                        if (epcTempStr.length() < 2)
                            epcTempStr = "0" + epcTempStr;


                        epcStr.append(epcTempStr);
                    }
//                    epcStr.append(epcTempChar);
                    LogUtil.getInstance().d("EPC:" + epcStr.toString());
                    inventoryInfoList.add(new InventoryInfo(
                            buffer[15] * 256 + buffer[16],
                            buffer[19] * 256 + buffer[20],
                            epcStr.toString()));

                    break;
                default:
                    break;
            }
        }
    }
}
