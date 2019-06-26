package com.zk.cabinet.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zk.cabinet.R;

/**
 * Created by ZK on 2018/1/9.
 */

public class CustomProgressDialog extends Dialog{
    private LayoutInflater inflater;
    private View v;
    private LinearLayout layout;
    private ImageView spaceshipImage;
    private TextView tipTextView;
    private Animation hyperspaceJumpAnimation;

    public CustomProgressDialog(@NonNull Context context) {
        super(context, R.style.loading_dialog);
        inflater = LayoutInflater.from(context);
        v = inflater.inflate(R.layout.dialog_loading, null);// 得到加载view
        layout = v.findViewById(R.id.dialog_view);// 加载布局
        spaceshipImage = v.findViewById(R.id.img);
        tipTextView = v.findViewById(R.id.tipTextView);// 提示文字
        hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.load_animation);
//        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
//        spaceshipImage.setAnimation(hyperspaceJumpAnimation);
        setCancelable(false);
        setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }
    }

    public void setMessage(String msg){
        if (tipTextView != null)
            tipTextView.setText(msg);
    }

    @Override
    public void show() {
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        super.show();
    }

    @Override
    public void dismiss() {
        hyperspaceJumpAnimation.cancel();
        super.dismiss();
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if (getOwnerActivity() != null){
            getOwnerActivity().dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    //    /**
//     * 得到自定义的progressDialog
//     * @param context
//     * @param msg
//     * @return_tools
//     */
//    public static Dialog createLoadingDialog(Context context, String msg) {
//
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
//        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
//        // main.xml中的ImageView
//        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
//        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
//        // 加载动画
//        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
//                context, R.anim.load_animation);
//        // 使用ImageView显示动画
//        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
//        tipTextView.setText(msg);// 设置加载信息
//
//        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog
//
//        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
//        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
//        return_tools loadingDialog;
//
//    }

}
