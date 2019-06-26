package com.zk.cabinet.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.zk.cabinet.R;

/**
 * Created by ZK on 2017/12/19.
 */

public class FullScreenAlertDialog extends AlertDialog {
    private Context context;

    public FullScreenAlertDialog(@NonNull Context context) {
        super(context, R.style.FullscreenDialog);
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }
//        this.context = context;
    }

//    public FullScreenAlertDialog(@NonNull Context context, int themeResId) {
//        super(context, themeResId);
//    }
//
//    public FullScreenAlertDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
//        super(context, cancelable, cancelListener);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void show() {
        super.show();

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity= Gravity.BOTTOM;
        layoutParams.width= LayoutParams.MATCH_PARENT;
        layoutParams.height= LayoutParams.MATCH_PARENT;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if (getOwnerActivity() != null){
            getOwnerActivity().dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }


}
