package com.zk.cabinet.util;

import android.content.Context;
import android.media.MediaPlayer;

import com.zk.cabinet.R;

import java.util.ArrayList;

public class MediaPlayerUtil {
    private Context mContext;
    private MediaPlayer mediaPlayer;
    private ArrayList<Integer> mediaPlayerList, numberList;


    private volatile static MediaPlayerUtil instance = null;

    private MediaPlayerUtil() {
        mediaPlayerList = new ArrayList<>();
        numberList = new ArrayList<>();
        numberList.add(R.raw.zero);
        numberList.add(R.raw.one);
        numberList.add(R.raw.two);
        numberList.add(R.raw.three);
        numberList.add(R.raw.four);
        numberList.add(R.raw.five);
        numberList.add(R.raw.six);
        numberList.add(R.raw.seven);
        numberList.add(R.raw.eight);
        numberList.add(R.raw.nine);
        numberList.add(R.raw.ten);
        numberList.add(R.raw.hundred);
    }

    public void init(Context mContext) {
        this.mContext = mContext;
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
     * 报数（只做了0-999的报数，如果有超过999的请重新修改）
     *
     * @param takeNumber 取出的本数
     * @param saveNumber 存入的本数
     */
    public void reportNumber(int takeNumber, int saveNumber, int errorNumber) {
        if (errorNumber == 0) {
            if (saveNumber != 0 && takeNumber != 0) {

                mediaPlayerList.add(R.raw.operation);
                mediaPlayerList.add(R.raw.out);
                if (takeNumber > 100) {
                    mediaPlayerList.add(numberList.get(takeNumber / 100));
                    mediaPlayerList.add(numberList.get(11));
                }
                if ((takeNumber % 100) > 10) {
                    takeNumber = takeNumber % 100;
                    mediaPlayerList.add(numberList.get(takeNumber / 10));
                    mediaPlayerList.add(numberList.get(10));
                    takeNumber = takeNumber % 10;
                } else if (takeNumber > 100 && (takeNumber % 100) > 0) {
                    takeNumber = takeNumber % 100;
                    mediaPlayerList.add(numberList.get(0));
                }
                if (takeNumber > 0) {
                    mediaPlayerList.add(numberList.get(takeNumber));
                }
                mediaPlayerList.add(R.raw.book);
                mediaPlayerList.add(R.raw.deposit);
                if (saveNumber > 100) {
                    mediaPlayerList.add(numberList.get(saveNumber / 100));
                    mediaPlayerList.add(numberList.get(11));
                }
                if ((saveNumber % 100) > 10) {
                    saveNumber = saveNumber % 100;
                    mediaPlayerList.add(numberList.get(saveNumber / 10));
                    mediaPlayerList.add(numberList.get(10));
                    saveNumber = saveNumber % 10;
                } else if (saveNumber > 100 && (saveNumber % 100) > 0) {
                    saveNumber = saveNumber % 100;
                    mediaPlayerList.add(numberList.get(0));
                }
                if (saveNumber > 0) {
                    mediaPlayerList.add(numberList.get(saveNumber));
                }
                mediaPlayerList.add(R.raw.book);

            } else if (saveNumber != 0 && takeNumber == 0) {

                mediaPlayerList.add(R.raw.operation);
                mediaPlayerList.add(R.raw.deposit);
                if (saveNumber > 100) {
                    mediaPlayerList.add(numberList.get(saveNumber / 100));
                    mediaPlayerList.add(numberList.get(11));
                }
                if ((saveNumber % 100) > 10) {
                    saveNumber = saveNumber % 100;
                    mediaPlayerList.add(numberList.get(saveNumber / 10));
                    mediaPlayerList.add(numberList.get(10));
                    saveNumber = saveNumber % 10;
                } else if (saveNumber > 100 && (saveNumber % 100) > 0) {
                    saveNumber = saveNumber % 100;
                    mediaPlayerList.add(numberList.get(0));
                }
                if (saveNumber > 0) {
                    mediaPlayerList.add(numberList.get(saveNumber));
                }
                mediaPlayerList.add(R.raw.book);

            } else if (saveNumber == 0 && takeNumber != 0) {

                mediaPlayerList.add(R.raw.operation);
                mediaPlayerList.add(R.raw.out);
                if (takeNumber > 100) {
                    mediaPlayerList.add(numberList.get(takeNumber / 100));
                    mediaPlayerList.add(numberList.get(11));
                }
                if ((takeNumber % 100) > 10) {
                    takeNumber = takeNumber % 100;
                    mediaPlayerList.add(numberList.get(takeNumber / 10));
                    mediaPlayerList.add(numberList.get(10));
                    takeNumber = takeNumber % 10;
                } else if (takeNumber > 100 && (takeNumber % 100) > 0) {
                    takeNumber = takeNumber % 100;
                    mediaPlayerList.add(numberList.get(0));
                }
                if (takeNumber > 0) {
                    mediaPlayerList.add(numberList.get(takeNumber));
                }
                mediaPlayerList.add(R.raw.book);

            } else if (saveNumber == 0 && takeNumber == 0) {
                mediaPlayerList.add(R.raw.nochange);
            }
        } else {
            switch (errorNumber) {
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
                default:
                    break;
            }
        }
        mediaPlayerListPlay();
    }

    public void stopReportNumber() {
        mediaPlayerList.clear();
        mediaPlayer.release();
    }

    private void mediaPlayerListPlay() {
        mediaPlayer = MediaPlayer.create(mContext, mediaPlayerList.remove(0));
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(onCompletionListener);
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mediaPlayer.release();
            if (mediaPlayerList.size() > 0) {
                mediaPlayerListPlay();
            }
        }
    };

}
