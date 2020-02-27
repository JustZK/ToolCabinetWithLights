package com.zk.cabinet.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.bean.InventoryAll;
import com.zk.cabinet.bean.InventoryInfo;
import com.zk.cabinet.bean.NettySendInfo;
import com.zk.cabinet.broadcast.AlarmEventReceiver;
import com.zk.cabinet.callback.InventoryListener;
import com.zk.cabinet.constant.SelfComm;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.db.InventoryAllService;
import com.zk.cabinet.netty.server.NettyServerParsingLibrary;
import com.zk.cabinet.network.NetworkRequest;
import com.zk.cabinet.util.LogUtil;
import com.zk.cabinet.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BusinessService extends Service {
    public static final String TAG = "BusinessService";
    public static final String HANDLER_THREAD_NAME = "BusinessThread";
    private final static int KEEP_LIVE = 0x01;

    // 用于向Activity发送数据
    private Messenger frontMessenger = null; //Activity的Messenger
    private Messenger businessMessenger; //BusinessService的Messenger


    private HandlerThread businessHandlerThread = null;
    private BusinessHandler businessHandler = null;

    private AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

    private SharedPreferencesUtil spUtil;

    private String mInventoryCoding;
    private int mCountErNumber;
    private String mEntireInventoryTime = "";
    private TimerTask taskKeepLive;
    private ScheduledExecutorService systemPool;
    private PendingIntent taskPendingIntent;
    private boolean isInventorying = false;
    private Object objectForInventory = new Object();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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
            case SelfComm.BUSINESS_ENTIRE_INVENTORY_ALARM:
                if (!isInventorying) {

                    executorService.submit(new CalibrationRunnable());


                }
                //TODO
                break;
            case KEEP_LIVE:
                Bundle bundle = receiveMessage.getData();
                mInventoryCoding = bundle.getString("InventoryCoding");
                String entireInventoryTimeTemp = bundle.getString("OperateTime");
                if (!mEntireInventoryTime.equals(entireInventoryTimeTemp)) {
                    if (taskPendingIntent != null && alarmManager != null){
                        alarmManager.cancel(taskPendingIntent);
                    }
                    mEntireInventoryTime = entireInventoryTimeTemp;
                    configTimingTask(AlarmEventReceiver.ACTION_ENTIRE_INVENTORY,
                            mEntireInventoryTime, businessMessenger);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        spUtil = SharedPreferencesUtil.getInstance();
        businessHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        businessHandlerThread.start();
        businessHandler = new BusinessHandler(businessHandlerThread.getLooper(), this);
        businessMessenger = new Messenger(businessHandler);

        taskKeepLive = new TimerTask() {
            @Override
            public void run() {
                LogUtil.getInstance().d(TAG, "taskKeepLive");
                keepLive(spUtil.getString(SharedPreferencesUtil.Key.DeviceId, ""),
                        spUtil.getString(SharedPreferencesUtil.Key.UnitNumber, ""));
            }
        };
        systemPool = Executors.newScheduledThreadPool(1);
        systemPool.scheduleAtFixedRate(taskKeepLive, 0, 5, TimeUnit.MINUTES);

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
     * 配置定时任务
     *
     * @param action        活动名称
     * @param taskTimeToday 触发时间
     * @param messenger     通信对象
     */
    private void configTimingTask(String action, String taskTimeToday, Messenger messenger) {
        if (alarmManager == null) {
            LogUtil.getInstance().d(TAG, "configTimingTask定时任务管理未初始化，alarmManager == null");
            return;
        }

        Calendar taskCalendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date;
        try {
            date = sdf.parse(taskTimeToday);
        } catch (ParseException e) {
            LogUtil.getInstance().d(TAG, "SimpleDateFormat ERROR");
            e.printStackTrace();
            return;
        }
        taskCalendar.setTime(date);
        long taskTime = taskCalendar.getTimeInMillis();
        LogUtil.getInstance().d(TAG, "new taskTime=" + taskTime);

        Intent taskIntent = new Intent(action);
        taskIntent.putExtra(SelfComm.BUSINESS_MESSENGER, messenger);
        taskIntent.setClass(this, AlarmEventReceiver.class);
        taskPendingIntent = PendingIntent.getBroadcast(this, 0, taskIntent, 0);

        if (Build.VERSION.SDK_INT < 19) {
            alarmManager.set(AlarmManager.RTC, taskTime, taskPendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC, taskTime, taskPendingIntent);
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

    private class CalibrationRunnable implements Runnable {

        @Override
        public void run() {
            isInventorying = true;
            if (frontMessenger != null) {
                Message sendMessage = Message.obtain();
                sendMessage.what = SelfComm.BUSINESS_ENTIRE_INVENTORY;
                sendMessage.obj = true;
                try {
                    frontMessenger.send(sendMessage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            List<Cabinet> cabinetList = CabinetService.getInstance().loadAll();
            InventoryAllService.getInstance().deleteAll();
            NettyServerParsingLibrary.getInstance().processor.onInventoryListener(mInventoryListener);
            for (Cabinet cabinet: cabinetList){
                String[] antennaNumberStr = cabinet.getAntennaNumber().split(",");
                for (String s : antennaNumberStr) {
                    int antennaNumber = Integer.valueOf(s);
                    NettyServerParsingLibrary.getInstance().send(new NettySendInfo(
                            cabinet.getReaderDeviceID(), 0,
                            antennaNumber, 0));

                    synchronized (objectForInventory) {
                        try {
                            objectForInventory.wait(20 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            NettyServerParsingLibrary.getInstance().processor.onInventoryListener(null);
            //上传数据
            upInventoryList(spUtil.getString(SharedPreferencesUtil.Key.DeviceId, ""),
                    spUtil.getString(SharedPreferencesUtil.Key.UnitNumber, ""), mInventoryCoding,
                    mEntireInventoryTime, InventoryAllService.getInstance().loadAll());
            isInventorying = false;
            if (frontMessenger != null) {
                Message sendMessage = Message.obtain();
                sendMessage.what = SelfComm.BUSINESS_ENTIRE_INVENTORY;
                sendMessage.obj = false;
                try {
                    frontMessenger.send(sendMessage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private InventoryListener mInventoryListener = new InventoryListener() {
        @Override
        public void inventoryList(int readerID, int result, List<InventoryInfo> inventoryInfoList) {
            List<InventoryAll> inventoryAllList = new ArrayList<>();
            for (InventoryInfo inventoryInfo : inventoryInfoList){
                InventoryAll inventoryAll = new InventoryAll();
                inventoryAll.setEPC(inventoryInfo.getEPC());
                inventoryAll.setRSSI(inventoryInfo.getRSSI());
                inventoryAll.setCountErNumber(mCountErNumber);
                inventoryAll.setReaderID(readerID);
                inventoryAll.setAntennaNumber(inventoryInfo.getAntennaNumber());
                inventoryAllList.add(inventoryAll);
            }
            InventoryAllService.getInstance().insert(inventoryAllList);

            synchronized (objectForInventory) {
                objectForInventory.notify();
            }
        }
    };

    private void keepLive(String cabinetID, String mechanismCoding) {
        String url = NetworkRequest.getInstance().urlHeartbeat;
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("CabinetID", cabinetID);
            jsonObject.put("MechanismCoding", mechanismCoding);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonResult) {
                try {
                    JSONObject jsonObject = jsonResult.getJSONObject("Data");
                    if (jsonResult.getInt("Result") == 200) {
                        Bundle bundle = new Bundle();
                        bundle.putString("InventoryCoding", jsonObject.getString("InventoryCoding"));
                        bundle.putString("OperateTime", jsonObject.getString("OperateTime"));
                        Message msg = Message.obtain();
                        msg.what = KEEP_LIVE;
                        msg.setData(bundle);
                        businessHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    LogUtil.getInstance().d(TAG, "JSONException:" + e.toString());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtil.getInstance().d(TAG, "onErrorResponse:" + error.toString());
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkRequest.getInstance().add(jsonObjectRequest);
    }

    private void upInventoryList(String cabinetID, String mechanismCoding, String inventoryCoding,
                                 String operateTime, List<InventoryAll> inventoryAllList) {
        String url = "";
        JSONObject jsonObject = new JSONObject();
        url = NetworkRequest.getInstance().urlInventory;
        try {
            jsonObject.put("CabinetID", cabinetID);
            jsonObject.put("MechanismCoding ", mechanismCoding);
            jsonObject.put("InventoryCoding", inventoryCoding);
            jsonObject.put("OperateTime", operateTime);

            JSONArray accessRecordJSONArray = new JSONArray();
            if (inventoryAllList != null) {
                for (InventoryAll inventoryAll : inventoryAllList)
                    accessRecordJSONArray.put(inventoryAll.getJSONObject());
            }
            jsonObject.put("Data", accessRecordJSONArray);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        LogUtil.getInstance().d("盘库上报：" + jsonObject);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonResult) {

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkRequest.getInstance().add(jsonObjectRequest);
    }
}
