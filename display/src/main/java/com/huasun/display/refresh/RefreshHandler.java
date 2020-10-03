package com.huasun.display.refresh;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.huasun.core.app.Latte;
import com.huasun.core.net.RestClient;
import com.huasun.core.net.callback.ISuccess;
import com.huasun.display.recycler.DataConverter;
import com.huasun.display.recycler.MultipleRecyclerAdapter;
import com.huasun.display.recycler.StickHeaderDecoration;

import java.util.HashMap;
import java.util.Map;


/**
 * author:songwenming
 * Date:2019/10/13
 * Description:
 */
public class RefreshHandler implements SwipeRefreshLayout.OnRefreshListener,BaseQuickAdapter.RequestLoadMoreListener{

    private Map<Integer,String> keys=new HashMap<>();//存放
    private final SwipeRefreshLayout REFRESH_LAYOUT;
    private final PagingBean BEAN;
    private final RecyclerView RECYCLERVIEW;
    private MultipleRecyclerAdapter mAdapter;
    private final DataConverter CONVERTER;

    private RefreshHandler(SwipeRefreshLayout refresh_layout,RecyclerView recyclerView,
                          DataConverter dataConverter,PagingBean pagingBean) {
        this.REFRESH_LAYOUT = refresh_layout;
        this.RECYCLERVIEW=recyclerView;
        this.CONVERTER=dataConverter;
        this.BEAN=pagingBean;
        REFRESH_LAYOUT.setOnRefreshListener(this);
    }

    public static RefreshHandler create(SwipeRefreshLayout refresh_layout,RecyclerView recyclerView,
                                        DataConverter dataConverter,PagingBean pagingBean){
        return new RefreshHandler(refresh_layout,recyclerView,dataConverter,pagingBean);
    }
    private void refresh(){
        REFRESH_LAYOUT.setRefreshing(true);
        Latte.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                REFRESH_LAYOUT.setRefreshing(false);
            }
        },2000);
    }



    public void initData (String json){
                        keys.put(0,"我是第0个标题");

                        mAdapter=MultipleRecyclerAdapter.create(CONVERTER.setJsonData(json));
                        mAdapter.setOnLoadMoreListener(RefreshHandler.this,RECYCLERVIEW);
                        RECYCLERVIEW.setAdapter(mAdapter);
                        //recyclerview将动滑动到底部
                        RECYCLERVIEW.scrollToPosition(mAdapter.getItemCount()-1);

    }

    @Override
    public void onRefresh() {

        refresh();
    }

    @Override
    public void onLoadMoreRequested() {
        mAdapter.loadMoreEnd(true);
        mAdapter.loadMoreComplete();
    }
}
