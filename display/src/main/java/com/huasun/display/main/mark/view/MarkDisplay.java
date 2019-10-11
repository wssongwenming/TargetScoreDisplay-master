package com.huasun.display.main.mark.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.huasun.display.R;

/**
 * author:songwenming
 * Date:2019/9/24
 * Description:
 靶纸规格：
 *ｃｘ位置为５０％位置
 * ｃｙ位置为６０％
 */
public class MarkDisplay extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    int i=0;
    private SurfaceHolder mHolder;
    /**
     * 与SurfaceHolder绑定的Canvas
     */
    private Canvas mCanvas;
    /**
     * 用于绘制的线程
     */
    private Thread t;
    /**
     * 线程的控制开关
     */
    private boolean isRunning;


    private int mRingConunt=5;
    /**
     * 圆的直径
     */
    private int mRadius;

    private Paint mRingPaint;

    /**
     * 绘制文字的画笔
     */
    private Paint mTextPaint;

    /**
     * 控件的中心位置
     */
    private int mCenter;
    /**
     * 靶心的x坐标位置
     */
    private int cx;
    /**
     * 靶心的y坐标位置
     */
    private int cy;
    /**
     * 靶心y坐标占整个高度的比例
     */
    private float cyRatio=(float)186/328;

    /**
     * 环间距，10环的半径
     */
    private int radiusUnit;

    private float radiusUnitRatio=0.1f;
    /**
     * 控件的padding，这里我们认为4个padding的值一致，以paddingleft为标准
     */
    private int mPadding;

    /**
     * 背景图的bitmap
     */
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(),
            R.drawable.targetback);
    /**
     * 文字的大小
     */
    private float mTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());


    public MarkDisplay(Context context) {
        super(context);
    }

    public MarkDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHolder = getHolder();
        mHolder.addCallback(this);

        // setZOrderOnTop(true);// 设置画布 背景透明
        // mHolder.setFormat(PixelFormat.TRANSLUCENT);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);

    }


    /**
     * 设置控件为正方形
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        Log.d("width", width+"");

        // 获取圆形的直径
        mRadius = width - getPaddingLeft() - getPaddingRight();
        Log.d("paddingleft",getPaddingLeft()+"");
        Log.d("paddingright",getPaddingRight()+"");
        Log.d("paddingtop",getPaddingTop()+"");
        Log.d("paddingbottom",getPaddingBottom()+"");
        Log.d("radius", mRadius+"");
        // padding值
        mPadding = getPaddingLeft();
        // 中心点
        mCenter = width / 2;

        Log.d("mCenter", mCenter+"");
        cx=mCenter;

        cy=(int)((0.6*mRadius)+getPaddingTop());

        radiusUnit=(int)(mRadius*radiusUnitRatio);
        Log.d("unit", radiusUnit+"");
        //正方形高宽
        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mRingPaint=new Paint();
        mRingPaint.setAntiAlias(true);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth((float) 2.0); //线宽
        mRingPaint.setDither(true);

        // 开启线程
        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 通知关闭线程
        isRunning = false;
    }

    @Override
    public void run() {

        // 不断的进行draw
        while (isRunning) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            try {
                if (end - start < 50) {
                    Thread.sleep(50 - (end - start));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private void draw() {
        try {
            // 获得canvas
            mCanvas = mHolder.lockCanvas();

            if (mCanvas != null) {
                // 绘制背景图
                drawBg();
                mRingPaint.setColor(0xFFFFFFFF);

                for (int i = 1; i <= 5; i++) {
                    // 绘制快快
                    //mArcPaint.setColor(mColors[i]);
//					mArcPaint.setStyle(Style.STROKE);
                    mCanvas.drawCircle(cx,cy,radiusUnit*i,mRingPaint);
                    //mCanvas.drawArc(mRange, tmpAngle, sweepAngle, true,mArcPaint);
                    // 绘制文本
                    //drawText(tmpAngle, sweepAngle, mStrs[i]);
                    // 绘制Icon
                    //drawIcon(tmpAngle, i);

                    //tmpAngle += sweepAngle;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null)
                mHolder.unlockCanvasAndPost(mCanvas);
        }

    }

    /**
     * 根据当前旋转的mStartAngle计算当前滚动到的区域 绘制背景，不重要，完全为了美观
     */
    private void drawBg() {
        mCanvas.drawColor(0xFFFFFFFF);
        mCanvas.drawBitmap(mBgBitmap, null, new Rect(mPadding / 2,
                mPadding / 2, getMeasuredWidth() - mPadding / 2,
                getMeasuredWidth() - mPadding / 2), null);
    }


}
