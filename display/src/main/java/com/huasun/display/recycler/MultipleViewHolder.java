package com.huasun.display.recycler;

import android.view.View;

import com.chad.library.adapter.base.BaseViewHolder;

/**
 * author:songwenming
 * Date:2019/10/14
 * Description:
 */
public class MultipleViewHolder extends BaseViewHolder {
    private MultipleViewHolder(View view) {
        super(view);
    }
    public static MultipleViewHolder create(View view){
        return new MultipleViewHolder(view);

    }
}
