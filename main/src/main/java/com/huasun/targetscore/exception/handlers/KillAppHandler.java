package com.huasun.targetscore.exception.handlers;


import com.huasun.targetscore.exception.IHandlerException;

/**
 * Created by songwenming on 2017/4/5.
 */

public class KillAppHandler implements IHandlerException {
    @Override
    public boolean handler(Throwable e) {
        android.os.Process.killProcess(android.os.Process.myPid());
        return true;
    }
}
