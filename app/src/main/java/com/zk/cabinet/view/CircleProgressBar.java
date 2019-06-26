package com.zk.cabinet.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.zk.cabinet.R;

/**
 * Created by ZK on 2017/12/15.
 */

public class CircleProgressBar extends View {
    // 画圆环背景的画笔
    private Paint ringBackgroundPaint;
    private Paint ringBackgroundImagePaint;
    // 画圆环的画笔
    private Paint ringPaint;
    // 画字体的画笔
    private Paint textPaint;
    // 画圆环背景颜色
    private int ringBackgroundColor;
    private int iconBackgroundImage;
    // 圆环颜色
    private int ringColor;
    // 字体颜色
    private int textColor;
    // 半径
    private float radius;
    // 圆环宽度
    private float strokeWidth;
    // 字的长度
    private float txtWidth;
    // 字的高度
    private float txtHeight;
    // 总进度
    private int totalProgress = 100;
    // 当前进度
    private int currentProgress;
    //透明度
    private int alpha = 100;

    private RectF oval;
    private Bitmap mBitmap;

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        initVariable();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleProgressbar, 0 , 0);
        radius = typeArray.getDimension(R.styleable.CircleProgressbar_radius, 80);
        strokeWidth = typeArray.getDimension(R.styleable.CircleProgressbar_strokeWidth, 10);
        ringBackgroundColor = typeArray.getColor(R.styleable.CircleProgressbar_ringBackgroundColor, 0xFF0000);
        iconBackgroundImage = typeArray.getResourceId(R.styleable.CircleProgressbar_ringBackgroundImage, R.drawable.fingerprint_prompts);
        ringColor = typeArray.getColor(R.styleable.CircleProgressbar_ringColor, 0xFF0000);
        textColor = typeArray.getColor(R.styleable.CircleProgressbar_textColor, 0xFFFFFF);
    }

    private void initVariable() {
        ringPaint = new Paint();
        ringPaint.setAntiAlias(true);
        ringPaint.setDither(true);
        ringPaint.setColor(ringColor);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        ringPaint.setStrokeWidth(strokeWidth);

        ringBackgroundImagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringBackgroundImagePaint.setFilterBitmap(true);
        ringBackgroundImagePaint.setDither(true);

        ringBackgroundPaint = new Paint();
        ringBackgroundPaint.setAntiAlias(true);
        ringBackgroundPaint.setDither(true);
        ringBackgroundPaint.setColor(ringBackgroundColor);
        ringBackgroundPaint.setStyle(Paint.Style.STROKE);
        ringBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        ringBackgroundPaint.setStrokeWidth(strokeWidth);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
        textPaint.setTextSize(radius/2);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        txtHeight = fm.descent + Math.abs(fm.ascent);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentProgress >= 0) {
            oval = new RectF(getWidth() / 2 - radius, getHeight() / 2 - radius, getWidth() / 2 + radius, getHeight() / 2 + radius);
            canvas.drawArc(oval, 0, 360, false, ringBackgroundPaint);
            canvas.drawArc(oval, -90, ((float) currentProgress / totalProgress) * 360, false, ringPaint);


//            String txt = currentProgress + "%";
//            txtWidth = textPaint.measureText(txt, 0, txt.length());
//            canvas.drawText(txt, getWidth() / 2 - txtWidth / 2, getHeight() / 2 + txtHeight / 4, textPaint);

            mBitmap = ((BitmapDrawable) getResources().getDrawable(iconBackgroundImage)).getBitmap();
            // 计算左边位置
            int left = (getWidth() - mBitmap.getWidth()) / 2;
            // 计算上边位置
            int top = (getWidth() - mBitmap.getHeight()) / 2;
            Rect mDestRect = new Rect(left, top, left + mBitmap.getWidth(), top + mBitmap.getHeight());
            canvas.drawBitmap(mBitmap, new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()), mDestRect, ringBackgroundImagePaint);
        }
    }

    public void setProgress(int progress) {
        currentProgress = progress;
        postInvalidate();
    }
}