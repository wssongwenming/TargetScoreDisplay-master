package com.huasun.targetscore.exception.handlers;

import android.app.Activity;

import com.huasun.targetscore.exception.IHandlerException;
import com.huasun.targetscore.exception.WindowManagerGlobal;


/**
 * Created by songwenming on 2017/4/5.
 */

public class EndCurrenPagerHandler implements IHandlerException {
    @Override
    public boolean handler(Throwable e) {
        Activity currenActivity = WindowManagerGlobal.getInstance().getCurrenActivity();
        if (currenActivity != null) {
            currenActivity.finish();
        }
        return false;
    }
}
