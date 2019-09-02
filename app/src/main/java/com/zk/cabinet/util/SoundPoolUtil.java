package com.zk.cabinet.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.zk.cabinet.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SoundPoolUtil {
    private volatile static SoundPoolUtil instance = null;
    private Map<Integer, Integer> soundPoolMap;
    private SoundPool soundPool;
    private Timer mTimer;

    private boolean isOpen = false;
    private Boolean isPlaying = false;
    private int number = 0;

    private int streamID;

    private SoundPoolUtil() {
    }

    public void init(Context context) {
        //4s
        soundPoolMap = new HashMap<>();
        soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
        soundPoolMap.put(0, soundPool.load(context, R.raw.nochange, 1));                      //本次无任何操作
        soundPoolMap.put(1, soundPool.load(context,R.raw.reader_offline, 1));                 //读写器离线
        soundPoolMap.put(2, soundPool.load(context,R.raw.only_allowed_out, 1));               //本次操作只允许取出
        soundPoolMap.put(3, soundPool.load(context,R.raw.error_removed_not_selected, 1));     //您取出了未选中的工具
        soundPoolMap.put(4, soundPool.load(context,R.raw.only_one_copy_allowed, 1));          //本次操作只允许归还一件工具
        soundPoolMap.put(5, soundPool.load(context,R.raw.only_allowed_deposit, 1));           //本次操作只允许存入
        soundPoolMap.put(6, soundPool.load(context,R.raw.deposit_does_not_match, 1));         //您存入的工具和您准备存入的工具不符
        soundPoolMap.put(7, soundPool.load(context,R.raw.deposited_multiple_tools, 1));       //您存入了多个工具
        soundPoolMap.put(8, soundPool.load(context,R.raw.successful_delivery, 1));            //取出成功
        soundPoolMap.put(9, soundPool.load(context,R.raw.successful_warehousing, 1));         //存入成功
        soundPoolMap.put(10, soundPool.load(context,R.raw.not_in_warehousing_list, 1));       //不在入库列表中

        soundPoolMap.put(11, soundPool.load(context,R.raw.incomplete_deposit, 1));            //未完成所有的存入
        soundPoolMap.put(12, soundPool.load(context,R.raw.unfinished, 1));                    //未完成所有的取出


        isOpen = SharedPreferencesUtil.getInstance().getBoolean(SharedPreferencesUtil.Key.BeepSound, true);

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isPlaying){
                    number ++;
                    if (number > 4) {
                        number = 0;
                        synchronized (isPlaying) {
                            isPlaying = false;
                        }
                    }
                }

            }
        }, 1000, 1000);
    }

    public void shutDown(){
        mTimer.cancel();
        soundPoolMap.clear();
        soundPool.release();
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public static SoundPoolUtil getInstance() {
        if (instance == null) {
            synchronized (SoundPoolUtil.class) {
                if (instance == null)
                    instance = new SoundPoolUtil();
            }
        }
        return instance;
    }

    public void reportNumber(int errorNumber) {
        if (!isOpen) return;
        if (errorNumber == 11){
            soundPool.stop(streamID);
            soundPool.play(soundPoolMap.get(errorNumber), 1.0f, 1.0f, 0, 0, 1);
        } else {
            if (isPlaying) return;
            streamID = soundPool.play(soundPoolMap.get(errorNumber), 1.0f, 1.0f, 0, 0, 1);
        }

        synchronized (isPlaying) {
            isPlaying = true;
        }

    }

}
