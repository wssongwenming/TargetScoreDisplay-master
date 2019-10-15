package com.huasun.display.main.mark;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huasun.core.app.Latte;
import com.huasun.core.delegates.bottom.BottomItemDelegate;
import com.huasun.display.R;
import com.huasun.display.R2;
import com.huasun.display.main.mark.view.MarkDisplay;
import com.huasun.display.recycler.MultipleItemEntity;
import com.huasun.display.refresh.PagingBean;
import com.huasun.display.refresh.RefreshHandler;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * author:songwenming
 * Date:2019/9/26
 * Description:
 */
public class MarkDelegate extends BottomItemDelegate {
    private ArrayList<MultipleItemEntity> medicineHistoryList=new ArrayList<>();
    int llcPersonDataHeight=0;
    int tvPersonDataHeight=0;
    int srlMarkHeight=0;

    @BindView(R2.id.surface_pan)
    MarkDisplay markDisplay=null;
    @BindView(R2.id.tv_person_data)
    AppCompatTextView mTvPersonData=null;
    @BindView(R2.id.llc_person_data)
    LinearLayoutCompat mLlcPersonData=null;
    @BindView(R2.id.rv_mark)
    RecyclerView mRecyclerView=null;
    @BindView(R2.id.srl_mark)
    SwipeRefreshLayout mRefreshLayout=null;

    private RefreshHandler mRefreshHandler;

    private void initRefreshLayout(){
        mRefreshLayout.setColorSchemeResources(
                android.R.color.background_dark,
                android.R.color.background_light
        );
        mRefreshLayout.setProgressViewOffset(true,llcPersonDataHeight+tvPersonDataHeight/2,llcPersonDataHeight+tvPersonDataHeight/2+200);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        initRefreshLayout();
        initRecyclerView();
        mRefreshHandler.firstPage("index");
    }
    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(linearLayoutManager);

    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public Object setLayout() {
        return R.layout.delegate_mark;
    }
    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        mRefreshHandler=RefreshHandler.create(mRefreshLayout,mRecyclerView,new MarkDataConverter(),new PagingBean());
        if(mRefreshLayout!=null) {
            mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refresh();
                }
            });
        }
        ViewTreeObserver observer = mLlcPersonData.getViewTreeObserver();
         observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
           @Override
            public boolean onPreDraw() {
                llcPersonDataHeight=mLlcPersonData.getHeight();
                tvPersonDataHeight=mTvPersonData.getHeight();
                srlMarkHeight=mRefreshLayout.getHeight();

                LinearLayoutCompat.LayoutParams lp = (LinearLayoutCompat.LayoutParams)mTvPersonData.getLayoutParams();
                lp.setMargins(30,-(llcPersonDataHeight+srlMarkHeight+20+tvPersonDataHeight/2),0,0);
                mTvPersonData.setLayoutParams(lp);
                return true;
            }
        });

    }
    private void refresh(){
        if(mRefreshLayout!=null)
            mRefreshLayout.setRefreshing(true);
        Latte.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //可以进行网络请求，REFESH_LAYOUT.setRefreshing(false);可以放入网络请求的success回调
                mRefreshLayout.setRefreshing(false);
            }
        },2000);
    }

}
