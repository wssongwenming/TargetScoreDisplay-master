package com.huasun.display.refresh;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.huasun.core.app.Latte;
import com.huasun.core.net.RestClient;
import com.huasun.core.net.callback.ISuccess;
import com.huasun.display.recycler.DataConverter;
import com.huasun.display.recycler.MultipleRecyclerAdapter;


/**
 * author:songwenming
 * Date:2019/10/13
 * Description:
 */
public class RefreshHandler implements SwipeRefreshLayout.OnRefreshListener,BaseQuickAdapter.RequestLoadMoreListener{
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

/*    public void injectDataIntoRecy (String url){
        BEAN.setDelayed(10);
        BEAN.setPageSize(20);
        RestClient.builder()
                .url(url)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response){
                        final JSONObject object= JSON.parseObject(response);
                        BEAN.setTotal(object.getInteger("total"));
                        mAdapter=MultipleRecyclerAdapter.create(CONVERTER.setJsonData(response));
                        mAdapter.setOnLoadMoreListener(RefreshHandler.this,RECYCLERVIEW);
                        RECYCLERVIEW.setAdapter(mAdapter);
                        BEAN.addIndex();
                    }
                })
                .build()
                .get();
    }*/

    public void initData (String json){
                        mAdapter=MultipleRecyclerAdapter.create(CONVERTER.setJsonData(json));
                        mAdapter.setOnLoadMoreListener(RefreshHandler.this,RECYCLERVIEW);
                        RECYCLERVIEW.setAdapter(mAdapter);

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
