package com.zk.cabinet.netty.base;

import com.zk.cabinet.bean.InventoryInfo;
import com.zk.cabinet.bean.NettySendInfo;
import com.zk.cabinet.callback.InventoryListener;
import com.zk.cabinet.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class NettyParsingLibrary {
    public static final String TAG = "NettyParsingLibrary";

    private String serverIp = "127.0.0.1";
    private int serverPort = 8888;

    private NettyClientBootstrap nettyClientBootstrap = null;
    private NettyClientHandler nettyClientHandler = null;
    private Processor processor = null;

    public NettyParsingLibrary(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        nettyClientHandler = new NettyClientHandler();
        nettyClientBootstrap = new NettyClientBootstrap(serverIp, serverPort, nettyClientHandler);

        processor = new Processor(nettyClientBootstrap);
        nettyClientHandler.setNettyClientEventProcessor(processor);

        new Thread() {
            @Override
            public void run() {
                nettyClientBootstrap.connect();
            }
        }.start();
    }

    public void reconnect() {
        if (processor != null && !processor.alive) {
            LogUtil.getInstance().d(TAG, "重新reconnect", false);
            nettyClientBootstrap.connect();
        }
    }

    public void send(NettySendInfo nettySendInfo) {
        switch (nettySendInfo.getCommunicationType()) {
            case 0x03:
                nettyClientBootstrap.send(NettyGroupPackage.inventory(nettySendInfo.getFastID(),
                        nettySendInfo.getAntennaNumber(), nettySendInfo.getInventoryType()));
                break;
        }
    }

    public void disConnect() {
        if (processor != null && processor.alive) {
            nettyClientBootstrap.disconnect();
        }
    }

    public void onInventoryListener(InventoryListener inventoryListener){
        if (processor != null) {
            processor.onInventoryListener(inventoryListener);
        }
    }

    private static class Processor extends NettyClientHandler.NettyClientEventProcessor {
        private byte[] remainBuffer = null;//上次解析剩余的数据
        private List<InventoryInfo> inventoryInfoList;
        private InventoryListener inventoryListener;

        private WeakReference<NettyClientBootstrap> nettyClientBootstrapWeakReference;
        private volatile int heartbeatLostCount = 0;
        private volatile boolean alive = false;

        // 触发三次接收闲时认为心跳断开
        private static final int HEARTBEAT_LOST = 3;

        private Processor(NettyClientBootstrap nettyClientBootstrap) {
            nettyClientBootstrapWeakReference = new WeakReference<>(nettyClientBootstrap);
        }

        public void setNettyClientBootstrap(NettyClientBootstrap nettyClientBootstrap) {
            nettyClientBootstrapWeakReference.clear();
            nettyClientBootstrapWeakReference = new WeakReference<>(nettyClientBootstrap);
        }

        public void onInventoryListener(InventoryListener inventoryListener){
            this.inventoryListener = inventoryListener;
        }

        @Override
        protected void onMessageReceived(byte[] buffer) {

            LogUtil.getInstance().d(TAG, "onMessageReceived");
            StringBuilder buffers = new StringBuilder();
            for (int i = 0; i < buffer.length; i++) {
                buffers.append(Integer.toHexString((buffer[i] & 0xff)));
                buffers.append(" ");
            }
            LogUtil.getInstance().d("test Received" + buffers);

            byte[] tempBytes;
            //如果上次解析有剩余，则将其加上
            if (remainBuffer != null && remainBuffer.length != 0) {
                tempBytes = NettyUtils.bytesMerger(remainBuffer, buffer);
            } else {
                tempBytes = buffer;
            }

            remainBuffer = interceptionReceivedData(tempBytes);

        }

        @Override
        protected void onWriteIdle() {
            LogUtil.getInstance().d(TAG, "onWriteIdle", false);
            if (nettyClientBootstrapWeakReference != null
                    && nettyClientBootstrapWeakReference.get() != null) {
                //发送心跳
                nettyClientBootstrapWeakReference.get().send(NettyGroupPackage.keepLive());
            }
        }

        @Override
        protected void onReadIdle() {
            LogUtil.getInstance().d(TAG, "onReadIdle", false);
            synchronized (Processor.class) {
                heartbeatLostCount++;
                if (heartbeatLostCount >= HEARTBEAT_LOST) {
                    if (nettyClientBootstrapWeakReference != null && nettyClientBootstrapWeakReference.get() != null) {
                        nettyClientBootstrapWeakReference.get().close();
                    }
                }
            }

        }

        @Override
        protected void onChannelActive() {
            LogUtil.getInstance().d(TAG, "onChannelActive", false);
            if (nettyClientBootstrapWeakReference != null
                    && nettyClientBootstrapWeakReference.get() != null) {
                //注册
                nettyClientBootstrapWeakReference.get().send(NettyGroupPackage.registered());
            }
        }

        @Override
        protected void onChannelInactive() {
            LogUtil.getInstance().d(TAG, "onChannelInactive", false);
            if (nettyClientBootstrapWeakReference != null && nettyClientBootstrapWeakReference.get() != null) {
                nettyClientBootstrapWeakReference.get().close();
            }
        }

        @Override
        protected void onClose() {
            LogUtil.getInstance().d(TAG, "onClose", false);
            alive = false;
//            if (communicationHandlerWeakReference != null && communicationHandlerWeakReference.get() != null) {
//                communicationHandlerWeakReference.get().sendEmptyMessage(SelfComm.COMMUNICATION_ONLINE_STATE);
//            }
        }

        /**
         * 截取完整的帧
         *
         * @param dataBytes 加上上次剩余的帧后的数据
         * @return 返回截取剩剩余的帧
         */
        private byte[] interceptionReceivedData(byte[] dataBytes) {
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
            if (headPosition1 != -1 && headPosition2 != -1 && tailPosition1 != -1 && tailPosition2 != -1) {
                if (tailPosition1 > headPosition2) {
                    byte[] tempCompleteBytes = new byte[tailPosition2 - headPosition1 + 1];
                    System.arraycopy(dataBytes, headPosition1, tempCompleteBytes, 0, tailPosition2 - headPosition1 + 1);
                    checkReceived(tempCompleteBytes, tailPosition2 - headPosition1 + 1);
                    //如果一组数据中有多个帧，则将剩余的数据发送
                    if (size > (tailPosition2 + 1)) {
                        byte[] subTempBytes = new byte[size - tailPosition2 - 1];
                        System.arraycopy(dataBytes, tailPosition2 + 1, subTempBytes, 0, subTempBytes.length);
                        return interceptionReceivedData(subTempBytes);
                    } else {
                        return null;
                    }
                } else {
                    byte[] subTempBytes = new byte[size - headPosition1];
                    System.arraycopy(dataBytes, headPosition1, subTempBytes, 0, subTempBytes.length);
                    return interceptionReceivedData(subTempBytes);
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
        private void checkReceived(byte[] buffer, int size) {
            LogUtil.getInstance().d("checkReceived");
            StringBuilder buffers = new StringBuilder();
            for (int i = 0; i < size; i++) {
                buffers.append(Integer.toHexString((buffer[i] & 0xff)));
                buffers.append(" ");
            }
            LogUtil.getInstance().d("SerialHelper", "test Received" + buffers);

            if (buffer[0] == NettyUtils.HEAD_HIGH && buffer[1] == NettyUtils.HEAD_LOW
                    && buffer[size - 2] == NettyUtils.TAIL_HIGH && buffer[size - 1] == NettyUtils.TAIL_LOW) {
                int T = NettyUtils.containCheck(buffer, size);
                if (T >= 0) {
                    byte[] tBuffer;
                    if (T > 0)
                        tBuffer = NettyUtils.translationForUnlock(buffer, size, T);
                    else
                        tBuffer = buffer;
                    //和校验
                    if (NettyUtils.andCheck(tBuffer, size - T)) {
                        //帧长度校验
                        if ((size - NettyUtils.HEAD_TAIL_NUMBER - T) == (tBuffer[2] & 0xff)) {
                            parser(tBuffer, size - T);
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
        private void parser(byte[] buffer, int size) {
            StringBuilder buffers = new StringBuilder();
            for (int i = 0; i < size; i++) {
                buffers.append(Integer.toHexString((buffer[i] & 0xff)));
                buffers.append(" ");
            }
            LogUtil.getInstance().d("test Received" + buffers);

            switch (buffer[6]) {
                case 0x01: //注册
                    nettyClientBootstrapWeakReference.get().send(NettyGroupPackage.registered());
                    break;
                case 0x03: //心跳

                    break;
                case 0x08: //返回Inventory 结果
                    if (buffer[10] == 0x00) {
                        if (inventoryInfoList == null) inventoryInfoList = new ArrayList<>();
                        else inventoryInfoList.clear();
                    } else if (buffer[10] == 0x01) {
                        //盘点结束
                        if (inventoryListener != null) {
                            inventoryListener.inventoryList(0,0, inventoryInfoList);
                        }
                    }
                    break;
                case 0x39: //Inventory 上报数据
                    StringBuilder epcStr = new StringBuilder();
                    int epcLength = buffer[29] * 256 + buffer[30];
                    char[] epcTempChar = new char[epcLength];
                    for (int i = 31; i < (31 + epcLength); i++) {
                        epcTempChar[i - 31] = (char) buffer[i];
                    }
                    epcStr.append(epcTempChar);
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
