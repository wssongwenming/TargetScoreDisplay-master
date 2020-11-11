package com.huasun.display.recycler;

import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.huasun.display.R;

import java.util.List;

/**
 * author:songwenming
 * Date:2019/10/14
 * Description:
 */
public class MultipleRecyclerAdapter extends BaseMultiItemQuickAdapter<MultipleItemEntity,MultipleViewHolder>{
    //设置图片加载策略
    private static final RequestOptions REQUEST_OPTIONS=
            new RequestOptions()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate();

    public MultipleRecyclerAdapter(List<MultipleItemEntity> data) {
        super(data);
        init();
    }
    protected static MultipleRecyclerAdapter create(List<MultipleItemEntity>data){
        return new MultipleRecyclerAdapter(data);
    }

    public static MultipleRecyclerAdapter create(DataConverter converter){
        return new MultipleRecyclerAdapter(converter.convert());

    }
    private void init(){
        addItemType(ItemType.TEXT_TEXT, R.layout.item_multiple_text);
        addItemType(ItemType.TEXT,R.layout.item_mutiple_text_text);
        //openLoadAnimation();
        //isFirstOnly(false);如果为false时Recyclerview 刷新时会闪
        isFirstOnly(true);

    }

    @Override
    protected MultipleViewHolder createBaseViewHolder(View view) {
        return MultipleViewHolder.create(view);
    }

    @Override
    protected void convert(MultipleViewHolder holder, MultipleItemEntity entity) {
        final String id;
        final String ringNumber;
        final String offsetDirection;
        final String shootingTime;
        switch (holder.getItemViewType()){
            case ItemType.TEXT_TEXT:
                id=entity.getField(MultipleFields.ID)+"";
                ringNumber=entity.getField(MultipleFields.RINGNUMBER);
                offsetDirection=entity.getField(MultipleFields.OFFSETDIRECTION);
                shootingTime=entity.getField(MultipleFields.SHOOTINGTIE);
                int postion=holder.getLayoutPosition();
                if(postion%2==0){
                    holder.setBackgroundColor(R.id.tv_shooting_time,0xFFECECEC);
                    holder.setBackgroundColor(R.id.tv_id,0xFFECECEC);
                    holder.setBackgroundColor(R.id.tv_ring_number,0xFFECECEC);
                    holder.setBackgroundColor(R.id.tv_offset_direction,0xFFECECEC);
                }else {
                    holder.setBackgroundColor(R.id.tv_id,Color.WHITE);
                    holder.setBackgroundColor(R.id.tv_ring_number,Color.WHITE);
                    holder.setBackgroundColor(R.id.tv_offset_direction,Color.WHITE);
                    holder.setBackgroundColor(R.id.tv_shooting_time,Color.WHITE);
                }
                holder.setText(R.id.tv_id,id);
                holder.setText(R.id.tv_ring_number,deleteZero(ringNumber));
                holder.setText(R.id.tv_offset_direction,offsetDirection);
                holder.setText(R.id.tv_shooting_time, shootingTime.substring(10,19));
                break;
        }

    }

    public  String deleteZero(String score){
        int indexofdot=score.indexOf(".");
        if(indexofdot>0) {
            String SCORE = score.substring(0, score.indexOf("."));
            return SCORE;
        }
        return score;
    }


}
