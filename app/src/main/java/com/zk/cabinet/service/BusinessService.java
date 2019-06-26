package com.zk.cabinet.service;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.zk.cabinet.constant.SelfComm;
import com.zk.cabinet.netty.NettyEntrance;
import com.zk.cabinet.util.LogUtil;

import java.lang.ref.WeakReference;

public class BusinessService extends Service {
    public static final String TAG = "BusinessService";
    public static final String HANDLER_THREAD_NAME = "BusinessThread";

    // 用于向Activity发送数据
    private Messenger frontMessenger = null; //Activity的Messenger
    private Messenger businessMessenger; //BusinessService的Messenger


    private HandlerThread businessHandlerThread = null;
    private BusinessHandler businessHandler = null;

    /**
     * 对应BusinessHandler的handleMessage
     *
     * @param receiveMessage 消息
     */
    private void handleMessage(Message receiveMessage) {
        Message sendMessage = null;

        switch (receiveMessage.what) {
            case SelfComm.BUSINESS_SERVICE_CONNECT:
                LogUtil.getInstance().d(TAG, "BUSINESS_SERVICE_CONNECT");
                frontMessenger = receiveMessage.replyTo;
                sendMessage = Message.obtain();
                sendMessage.what = SelfComm.BUSINESS_SERVICE_FRONT;
                try {
                    if (frontMessenger != null)
                        frontMessenger.send(sendMessage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case SelfComm.BUSINESS_SERVICE_NOW:
                // 修改当前通信的前端activity
                frontMessenger = receiveMessage.replyTo;
                break;
            default:
                break;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        businessHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        businessHandlerThread.start();
        businessHandler = new BusinessHandler(businessHandlerThread.getLooper(), this);
        businessMessenger = new Messenger(businessHandler);

        NettyEntrance nettyEntrance = NettyEntrance.getInstance();
        nettyEntrance.init();

        return businessMessenger.getBinder();
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);

        if (businessHandlerThread != null && businessHandlerThread.isAlive()) {
            if (Build.VERSION.SDK_INT >= 18) {
                businessHandlerThread.quitSafely();
            } else {
                businessHandlerThread.quit();
            }
        }
    }

    /**
     * BusinessHandler与View层进行交互
     */
    private static class BusinessHandler extends Handler {

        private final WeakReference<BusinessService> businessServiceWeakReference;

        BusinessHandler(BusinessService businessService) {
            super();
            businessServiceWeakReference = new WeakReference<>(businessService);

        }

        BusinessHandler(Looper looper, BusinessService businessService) {
            super(looper);
            businessServiceWeakReference = new WeakReference<>(businessService);

        }

        @Override
        public void handleMessage(Message msg) {
            if (businessServiceWeakReference.get() != null) {
                businessServiceWeakReference.get().handleMessage(msg);
            }
        }
    }
}
