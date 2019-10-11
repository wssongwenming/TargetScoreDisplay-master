package com.huasun.display.main.mark;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

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

    @BindView(R2.id.surface_pan)
    MarkDisplay markDisplay=null;
    @Override
    public Object setLayout() {
        return R.layout.delegate_mark;
    }
    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }

}
