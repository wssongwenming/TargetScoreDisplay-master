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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huasun.display.R;
import com.huasun.display.recycler.ItemType;
import com.huasun.display.recycler.MultipleFields;
import com.huasun.display.recycler.MultipleItemEntity;

/**
 * author:songwenming
 * Date:2019/9/24
 * Description:
 靶纸规格：
 *ｃｘ位置为５０％位置
 * ｃｙ位置为６０％
 */
public class MarkDisplay extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private String markJson="";
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

    private String[] mRingNumbers = new String[]{"6", "7", "8", "9", "10", "9","8","7","6"};
    private float[] mSpaceTimes= new float[]{0,1,2,3,4.5f,6,7,8,9};

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
     * 绘制10环文字的画笔
     */
    private Paint mTextTenPaint;

    private Paint mMarkPaint;
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
     * 环间距，10环的半径
     */
    private int radiusUnit;

    private float radiusUnitRatio=0.1f;
     /**
     * 背景图的bitmap
     */
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(),
            R.drawable.targetback);
    /**
     * 文字的大小
     */
    private float mTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 40, getResources().getDisplayMetrics());

    private float mMarkTextSize = TypedValue.applyDimension(
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
        mRadius = width;
        // 中心点
        mCenter = width / 2;

        cx=mCenter;

        cy=(int)((0.6*mRadius)+getPaddingTop());

        radiusUnit=(int)(mRadius*radiusUnitRatio);
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

        // 初始化绘制文字的画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(0xFFFFFFFF);
        mTextPaint.setTextSize(mTextSize);

        // 初始化绘制10环文字的画笔
        mTextTenPaint = new Paint();
        mTextTenPaint.setColor(0xFF000000);
        mTextTenPaint.setTextSize(mTextSize);
        //初始化绘制弹孔的画笔
        mMarkPaint = new Paint();
        mMarkPaint.setColor(0xFFFF0000);
        mMarkPaint.setTextSize(mMarkTextSize);
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
                drawRings(mCanvas);
                drawRingNumber(mCanvas);
                if(markJson!=null&&!markJson.isEmpty()){
                    drawMark();//
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null)
                mHolder.unlockCanvasAndPost(mCanvas);
        }

    }
    private void drawMark() {
        final JSONArray dataArray = JSON.parseObject(markJson).getJSONArray("data");
        final int size = dataArray.size();
        for (int i = 0; i < size; i++) {
            final JSONObject data = dataArray.getJSONObject(i);
            final int id = data.getInteger("id");
            final float r = data.getFloat("r");
            final float theta = data.getFloat("theta");


        }

    }
    private void drawRingNumber(Canvas mCanvas) {
        float n=0;
        for(int i=0;i<mRingNumbers.length;i++){
            String text=mRingNumbers[i];
            float spaceTimes=mSpaceTimes[i];
            Rect rect = new Rect();
            mTextPaint.getTextBounds(text,0,text.length(),rect);
            int textWidth=rect.width();
            int textHeight=rect.height();
            Log.d("markjson",markJson+"");
            if(i==4){
                mCanvas.drawText(text, (radiusUnit / 2 + spaceTimes * radiusUnit - textWidth / 2), cy + textHeight / 2, mTextTenPaint);
            }else {
                mCanvas.drawText(text, (radiusUnit / 2 + spaceTimes * radiusUnit - textWidth / 2), cy + textHeight / 2, mTextPaint);
            }
        }
    }
    private void drawRings(Canvas canvas) {
        mRingPaint.setColor(0xFFFFFFFF);
        mRingPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawCircle(cx,cy,radiusUnit*1,mRingPaint);
        mRingPaint.setStyle(Paint.Style.STROKE);
        for (int i = 2; i <= mRingConunt; i++) {
            mCanvas.drawCircle(cx,cy,radiusUnit*i,mRingPaint);
        }
    }
    /**
     * 根据当前旋转的mStartAngle计算当前滚动到的区域 绘制背景，不重要，完全为了美观
     */
    private void drawBg() {
        mCanvas.drawColor(0xFFFFFFFF);
        mCanvas.drawBitmap(mBgBitmap, null, new Rect(0,
                0, getMeasuredWidth(),
                getMeasuredHeight()), null);
    }
    public void setMarkJson(String markJson) {
        this.markJson = markJson;
    }
}
