package com.zk.cabinet.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.zk.cabinet.R;

import java.util.HashMap;
import java.util.Map;

public class MediaPlayerUtil {
    private SoundPool soundPool;
    private Map<Integer, Integer> map = new HashMap<>();

    private volatile static MediaPlayerUtil instance = null;

    private MediaPlayerUtil() {
    }

    public void init(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //传入音频的数量
            builder.setMaxStreams(1);
            //AudioAttributes是一个封装音频各种属性的类
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //设置音频流的合适属性
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            builder.setAudioAttributes(attrBuilder.build());
            soundPool = builder.build();
        } else {
            //第一个参数是可以支持的声音数量，第二个是声音类型，第三个是声音品质
            soundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        }
        map.put(1, soundPool.load(context, R.raw.reader_offline, 1));
        map.put(2, soundPool.load(context, R.raw.only_allowed_out, 1));
        map.put(3, soundPool.load(context, R.raw.error_removed_not_selected, 1));
        map.put(4, soundPool.load(context, R.raw.only_one_copy_allowed, 1));
        map.put(5, soundPool.load(context, R.raw.only_allowed_deposit, 1));
        map.put(6, soundPool.load(context, R.raw.deposit_does_not_match, 1));
        map.put(7, soundPool.load(context, R.raw.deposited_multiple_tools, 1));

    }

    public static MediaPlayerUtil getInstance() {
        if (instance == null) {
            synchronized (MediaPlayerUtil.class) {
                if (instance == null)
                    instance = new MediaPlayerUtil();
            }
        }
        return instance;
    }

    /**
     * 0://操作成功
     * 1://读写器离线
     * 2://本次操作只允许取出
     * 3://您取出了未选中的工具
     * 4://本次操作只允许归还一件工具
     * 5://本次操作只允许存入
     * 6://您存入的工具和您准备存入的工具不符
     * 7://您存入了多个工具
     */
    public void reportNumber(int number) {
        //第一个参数Context,第二个参数资源Id，第三个参数优先级
//        if (soundPool != null && number != 0) {
//            soundPool.play(map.get(number), 1.0f, 1.0f, 0, 0, 1);
//        }
    }

}


