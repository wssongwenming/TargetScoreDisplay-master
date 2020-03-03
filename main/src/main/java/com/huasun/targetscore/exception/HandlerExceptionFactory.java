package com.huasun.targetscore.exception;


import com.huasun.targetscore.exception.handlers.EndCurrenPagerHandler;
import com.huasun.targetscore.exception.handlers.IgnoreHandler;
import com.huasun.targetscore.exception.handlers.KillAppHandler;

/**
 * Created by zhangzheng on 2017/4/5.
 */

public class HandlerExceptionFactory implements IHandlerExceptionFactory {
    @Override
    public IHandlerException get(Throwable e) {
        if(e instanceof IllegalStateException){
            return new EndCurrenPagerHandler();
        }
        if(e instanceof SecurityException){
            return new KillAppHandler();
        }

        return new IgnoreHandler();
    }
}
