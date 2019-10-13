package com.huasun.display.main.mark;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huasun.core.delegates.bottom.BottomItemDelegate;
import com.huasun.display.R;
import com.huasun.display.R2;
import com.huasun.display.main.mark.view.MarkDisplay;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * author:songwenming
 * Date:2019/9/26
 * Description:
 */
public class MarkDelegate extends BottomItemDelegate {

    //@BindView(R2.id.surface_pan)
    //MarkDisplay markDisplay=null;
    @BindView(R2.id.tv_person_data)
    AppCompatTextView mTvPersonData=null;
    @BindView(R2.id.llc_person_data)
    LinearLayoutCompat mLlcPersonData=null;


    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);



    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_mark;
    }
    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

        ViewTreeObserver observer = mLlcPersonData.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int llcPersonDataHeight=mLlcPersonData.getHeight();
                int tvPersonDataHeight=mTvPersonData.getHeight();

                LinearLayoutCompat.LayoutParams lp = (LinearLayoutCompat.LayoutParams)mTvPersonData.getLayoutParams();
                lp.setMargins(30,-(llcPersonDataHeight+tvPersonDataHeight/2),0,0);
                mTvPersonData.setLayoutParams(lp);
                return true;
            }
        });
    }

}
