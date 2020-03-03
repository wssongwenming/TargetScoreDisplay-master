package com.huasun.targetscore.exception;

/**
 * Created by zhangzheng on 2017/4/5.
 */

public interface OnExceptionCallBack {

    void onThrowException(Thread t, Throwable e, IHandlerException handler);
}
