package com.huasun.display.launcher;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Chronometer;

import com.huasun.core.delegates.bottom.BottomItemDelegate;
import com.huasun.core.ui.launcher.ILauncherListener;
import com.huasun.core.ui.launcher.OnLauncherFinishTag;
import com.huasun.display.R;
import com.huasun.display.R2;

import java.util.Timer;

import butterknife.BindView;

/**
 * author:songwenming
 * Date:2018/9/24
 * Description:
 */
public class AfterFinishDelegate extends BottomItemDelegate {
    @BindView(R2.id.launcher_timer)
    Chronometer chronometer=null;
    //private int mCount=5;
    private int mCount=0;
    private Timer mTimer=null;
    private ILauncherListener mILauncherListener=null;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof ILauncherListener){
            mILauncherListener=(ILauncherListener) activity;

        }
    }

    @Override
    public Object setLayout() {
        return R.layout.after_finish_launcher;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        chronometer.setFormat("计时 %s");
        chronometer.start();
        //initTimer();
    }
    //判断是否显示滑动气动页




    private void checkSignIn(OnLauncherFinishTag onLauncherFinishTag){
        mILauncherListener.onLauncherFinish(onLauncherFinishTag);
    }
/*    @Override
    public void onTimer() {
        getProxyActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mTvTimer!=null){
                    if(mILauncherListener.getStatus()==2){
                        mTvTimer.setText(MessageFormat.format("请稍候\n{0}s",mCount));
                        mCount++;
                    }else if(mILauncherListener.getStatus()==0){
                        if(mTimer!=null){
                            mTimer.cancel();
                            mTimer=null;
                            checkSignIn(OnLauncherFinishTag.SIGNIN_BY_PASS);
                        }
                    }else if(mILauncherListener.getStatus()==1){
                        if(mTimer!=null){
                            mTimer.cancel();
                            mTimer=null;
                            checkSignIn(OnLauncherFinishTag.SIGNIN_BY_FACE);
                        }
                    }
                }
            }
        });
    }*/



}
