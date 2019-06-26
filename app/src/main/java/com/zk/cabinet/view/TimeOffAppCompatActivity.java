package com.zk.cabinet.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.zk.cabinet.util.LogUtil;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.util.SharedPreferencesUtil.Key;
import com.zk.cabinet.util.ToastUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by ZK on 2017/12/22.
 */

public class TimeOffAppCompatActivity extends AppCompatActivity {
    protected boolean isShow = true, isFinish = true;
    protected SharedPreferencesUtil spUtil;
    protected final int REQUEST_CODE = 0xFF;
    protected final int RESULT_CODE = 0xFE;
    private long temp;
    protected int mCountdown = -1, mCountdownTemp;
    private CountDownTimer timer;
    protected boolean isDialogShow = false;
    private static Toast toast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        spUtil = SharedPreferencesUtil.getInstance();

        if (isFinish) {
            setTimeKeep();
        }
    }

    private void setTimeKeep() {
        mCountdownTemp = spUtil.getInt(Key.Countdown, 60);
        if (mCountdownTemp != mCountdown) {
            mCountdown = mCountdownTemp;
            initTime();
        }
    }

    protected void timerStart() {
        if (timer != null)
            timer.start();
    }

    protected void timerCancel() {
        if (timer != null)
            timer.cancel();
    }

    protected void initTime() {
        timerCancel();
        timer = new CountDownTimer(mCountdown * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                temp = (millisUntilFinished / 1000);
                LogUtil.getInstance().d("倒计时", "------------------" + temp);
                countDownTimerOnTick(temp);
                if (temp <= 10) {
                    ToastUtil.showText(TimeOffAppCompatActivity.this, "已经" + (mCountdown - temp) + "秒无人操作" + temp + "秒后返回主界面");
                }
            }

            @Override
            public void onFinish() {
                if (isFinish) {
                    setResult(RESULT_CODE);
                    finish();
                }
            }
        };
    }

    protected void countDownTimerOnTick(long millisUntilFinished){

    }

    private void setKeyBortListener() {
        final View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > 400)) {

                    // ToastUtil.showText(TimeOffAppCompatActivity.this, "监听到软键盘弹起...");

                } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > 400)) {

                    //   ToastUtil.showText(TimeOffAppCompatActivity.this, "监听到软件盘关闭...");

                }

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void showTimerMessageEvent(String str) {
        if (str.equals("stop"))
            timer.cancel();
        else if (str.equals("start"))
            timer.start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            //获取触摸动作，如果ACTION_UP，计时开始。
            case MotionEvent.ACTION_UP:
                LogUtil.getInstance().d("---------------------MotionEvent.ACTION_UP");
                if (isFinish && isShow)
                    timerStart();

                break;
            //否则其他动作计时取消
            default:
                LogUtil.getInstance().d("---------------------MotionEvent.ACTION_Down");
                if (isFinish)
                    timerCancel();
                break;
        }
        if (isDialogShow)
            return false;
        else
            return super.dispatchTouchEvent(ev);
    }

    protected void IntentActivity(Class c) {
        Intent intent = new Intent(TimeOffAppCompatActivity.this, c);
        startActivityForResult(intent, REQUEST_CODE);
    }

    protected void IntentActivity(Class c, Bundle extras) {
        Intent intent = new Intent(TimeOffAppCompatActivity.this, c);
        intent.putExtras(extras);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isShow = true;
        if (isFinish && timer != null) {
            if (spUtil != null)
                setTimeKeep();
            timerStart();
        }
        LogUtil.getInstance().d("-----Activity onStart-----");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.getInstance().d("-----Activity onRestart-----");
    }

    @Override
    protected void onResume() {
        super.onResume();

        LogUtil.getInstance().d("-----Activity onResume-----");
    }

    @Override
    protected void onPause() {
        super.onPause();

        LogUtil.getInstance().d("-----Activity onPause-----");
    }

    @Override
    protected void onStop() {
        super.onStop();
        isShow = false;
        if (isFinish)
            timerCancel();
        LogUtil.getInstance().d("-----Activity onStop-----");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.getInstance().d("-----Activity onDestroy-----");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.getInstance().d("-----GuideActivity onActivityResult-----");
        if (isFinish && requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
            setResult(RESULT_CODE);
            finish();
        }
    }

    protected void showToast (CharSequence charSequence) {
//        Toast.makeText(TimeOffAppCompatActivity.this, charSequence, Toast.LENGTH_SHORT).show();
        if (toast == null) {
            toast = Toast.makeText(TimeOffAppCompatActivity.this.getApplicationContext(), "", Toast.LENGTH_SHORT);
        }
        toast.setText(charSequence);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
