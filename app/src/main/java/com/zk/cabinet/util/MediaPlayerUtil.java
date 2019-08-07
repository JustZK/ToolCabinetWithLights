package com.zk.cabinet.util;

import android.content.Context;
import android.media.MediaPlayer;

import com.zk.cabinet.R;

import java.util.ArrayList;

public class MediaPlayerUtil {
    private Context mContext;
    private MediaPlayer mediaPlayer;
    private ArrayList<Integer> mediaPlayerList;


    private volatile static MediaPlayerUtil instance = null;

    private boolean isPlaying = false;

    private boolean isOpen = false;

    private MediaPlayerUtil() {
        mediaPlayerList = new ArrayList<>();
    }

    public void init(Context mContext) {
        this.mContext = mContext;
        isOpen = SharedPreferencesUtil.getInstance().getBoolean(SharedPreferencesUtil.Key.BeepSound, true);
    }

    public void setOpen(boolean open) {
        isOpen = open;
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
     *
     */
    public void reportNumber(int errorNumber) {
        if (!isOpen) return;
        if (isPlaying) return;
        isPlaying = true;
        mediaPlayerList.clear();

        switch (errorNumber) {
            case 0://本次无任何操作
                mediaPlayerList.add(R.raw.nochange);
                break;
            case 1://读写器离线
                mediaPlayerList.add(R.raw.reader_offline);
                break;
            case 2://本次操作只允许取出
                mediaPlayerList.add(R.raw.only_allowed_out);
                break;
            case 3://您取出了未选中的工具
                mediaPlayerList.add(R.raw.error_removed_not_selected);
                break;
            case 4://本次操作只允许归还一件工具
                mediaPlayerList.add(R.raw.only_one_copy_allowed);
                break;
            case 5://本次操作只允许存入
                mediaPlayerList.add(R.raw.only_allowed_deposit);
                break;
            case 6://您存入的工具和您准备存入的工具不符
                mediaPlayerList.add(R.raw.deposit_does_not_match);
                break;
            case 7://您存入了多个工具
                mediaPlayerList.add(R.raw.deposited_multiple_tools);
                break;
            case 8://取出成功
                mediaPlayerList.add(R.raw.successful_delivery);
                break;
            case 9://存入成功
                mediaPlayerList.add(R.raw.successful_warehousing);
                break;
            default:
                break;

        }

        mediaPlayerListPlay();

    }

    public void stopReportNumber() {
        mediaPlayerList.clear();
        mediaPlayer.release();
    }

    private void mediaPlayerListPlay() {
        if (mediaPlayerList.size() <= 0) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer = MediaPlayer.create(mContext, mediaPlayerList.remove(0));
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(onCompletionListener);
                    } else {
                        isPlaying = false;
                    }
                } catch (Exception e) {
                    isPlaying = false;
                }
            }
        }).start();
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mediaPlayer.release();
            if (mediaPlayerList.size() > 0) {
                mediaPlayerListPlay();
            } else {
                isPlaying = false;
            }
        }
    };

}
