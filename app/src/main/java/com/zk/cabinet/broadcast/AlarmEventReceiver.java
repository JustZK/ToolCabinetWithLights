package com.zk.cabinet.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;

import com.zk.cabinet.constant.SelfComm;
import com.zk.cabinet.util.LogUtil;

public class AlarmEventReceiver extends BroadcastReceiver {

    public static final String TAG = "AlarmEventReceiver";
    public static final String ACTION_ENTIRE_INVENTORY = "com.zk.cabinet.ENTIRE_INVENTORY";

    private static final int INTERVAL = 24 * 60 * 60 * 1000;

    @Override
    public void onReceive(final Context context, Intent intent) {

        LogUtil.getInstance().d(TAG, "onReceive", false);
        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(ACTION_ENTIRE_INVENTORY)) {

            Messenger messenger = null;
            try {
                messenger = intent.getParcelableExtra(SelfComm.BUSINESS_MESSENGER);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.getInstance().d(TAG, "ACTION_REBOOT, intent.getParcelableExtra(SelfComm.BUSINESS_MESSENGER) ERROR");
            }

            // 收到整柜盘点广播
            LogUtil.getInstance().d(TAG, ACTION_ENTIRE_INVENTORY, false);

            if (messenger == null) {
                LogUtil.getInstance().d(TAG, "ACTION_REBOOT, messenger == null");
            } else {
                Message message = Message.obtain();
                message.what = SelfComm.BUSINESS_ENTIRE_INVENTORY_ALARM;
                try {
                    messenger.send(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.getInstance().d(TAG, "ACTION_REBOOT, messenger.send(message) ERROR");
                }
            }


        }


    }


}
