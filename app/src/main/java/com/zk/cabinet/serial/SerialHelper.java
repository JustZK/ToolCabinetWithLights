package com.zk.cabinet.serial;

import com.zk.cabinet.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPort;

/**
 * Created by ZK on 2018/1/2.
 * 串口辅助工具类
 */
public abstract class SerialHelper{
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private SendThread mSendThread;

    private String sPort="/dev/ttyHSL1";
    private int iBaudRate=115200;

    private boolean isOpen=false;
    private boolean sendFlag = true;

    private List<byte[]> sendList;
    private Object addSendSyncLock;

    public void open() throws SecurityException, IOException,InvalidParameterException {
        if (!isOpen) {

            addSendSyncLock = new Object();
            sendList = new ArrayList<>();

            isOpen = true;

            mSerialPort = new SerialPort(new File(sPort), iBaudRate, 0);

            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

            mReadThread = new ReadThread();
            mReadThread.start();

            mSendThread = new SendThread();
            mSendThread.start();
        }

    }

    /**
     * 1、结束读取线程
     * 2、关闭SerialPort
     */
    public void close(){
        if (mReadThread != null)
            mReadThread.interrupt();
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        isOpen=false;
    }

    /**
     * 结束写的线程
     */
    public void stopSend()
    {
        if (mSendThread != null)
            mSendThread.interrupt();
        sendFlag = false;
        if (addSendSyncLock != null) {
            synchronized (addSendSyncLock) {
                addSendSyncLock.notify();
            }
        }
    }

    /**
     * 写
     * @param bOutArray
     */
    private void send(byte[] bOutArray){
        StringBuilder buffers = new StringBuilder();
        for (int i = 0; i < bOutArray.length; i++) {
            buffers.append(Integer.toHexString((bOutArray[i] & 0xff)));
            buffers.append(" ");
        }
        LogUtil.getInstance().d("SerialHelper", "test 发送" + buffers);

        try
        {
            mOutputStream.write(bOutArray);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 读取线程
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                try
                {
                    if (mInputStream == null) return;
                    byte[] buffer=new byte[4096];
                    int size = mInputStream.read(buffer);
                    if (size > 0){
                        onDataReceived(sPort, buffer, size);
                    }
                } catch (Throwable e)
                {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * 写的线程
     */
    private class SendThread extends Thread {
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                while (sendFlag) {
                    if (sendList == null || sendList.size() == 0) {
                        try {
                            synchronized (addSendSyncLock) {
                                addSendSyncLock.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        byte[] send = sendList.remove(0);
                        if (send != null) {

                            StringBuilder buffers = new StringBuilder();
                            for (int i = 0; i < send.length; i++) {
                                buffers.append(Integer.toHexString((send[i] & 0xff)));
                                buffers.append(" ");
                            }
                            LogUtil.getInstance().LogPrint("串口通信Android发送：" + buffers);

                            send(send);
                        }
                        try {
                            SendThread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    /**
     * 添加写入的队列
     * @param sendDate
     */
    public void addSendTask(byte[] sendDate) {
//        StringBuilder buffers = new StringBuilder();
//        for (int i = 0; i < sendDate.length; i++) {
//            buffers.append(Integer.toHexString((sendDate[i] & 0xff)));
//            buffers.append(" ");
//        }
//        LogUtil.d("SerialHelper", "test Send" + buffers);

        sendList.add(sendDate);
        if (sendList.size() >= 1)
            synchronized (addSendSyncLock) {
                addSendSyncLock.notify();
            }
    }

    /**
     * 获取频率
     * @return
     */
    public int getBaudRate()
    {
        return iBaudRate;
    }

    /**
     * 设置频率
     * @param iBaud
     * @return
     */
    public boolean setBaudRate(int iBaud) {
        if (isOpen) {
            return false;
        } else {
            iBaudRate = iBaud;
            return true;
        }
    }

    /**
     * 设置频率
     * @param sBaud
     * @return
     */
    public boolean setBaudRate(String sBaud) {
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }

    /**
     * 获取串口路径
     * @return
     */
    public String getPort()
    {
        return sPort;
    }

    /**
     * 设置串口路径
     * @param sPort
     * @return
     */
    public boolean setPort(String sPort) {
        if (isOpen) {
            return false;
        } else {
            this.sPort = sPort;
            return true;
        }
    }

    /**
     * 是否打开读写线程
     * @return
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 收到的数据回调
     * @param sPort 串口端口
     * @param buffer 数据
     * @param size  长度
     */
    protected abstract void onDataReceived(String sPort, byte[] buffer, int size);
}