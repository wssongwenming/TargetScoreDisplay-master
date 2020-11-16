package com.huasun.core.app;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import java.util.HashMap;
import java.util.Queue;

/**
 * author:songwenming
 * Date:2019/9/21
 * Description:
 */
public final class Latte {
    public static Configurator init(Context context){
        getConfigurations().put(ConfigKeys.APPLICATION_CONTEXT,context.getApplicationContext());
        return Configurator.getInstance();
    }

    public static HashMap<Object,Object> getConfigurations(){
        return Configurator.getInstance().getLatteConfigs();
    }
    public static Context getApplication(){
        return (Context) getConfigurations().get(ConfigKeys.APPLICATION_CONTEXT);
    }
    public static Configurator getConfigurator() {
        return Configurator.getInstance();
    }
    public static <T> T getConfiguration(Object key) {
        return getConfigurator().getConfiguration(key);
    }
    public static Application getApplicationContext() {
        return getConfiguration(ConfigKeys.APPLICATION_CONTEXT);
    }
    public static Handler getHandler() {
        return getConfiguration(ConfigKeys.HANDLER);
    }
    public static Queue getQueue(){
        return (Queue) getConfigurations().get(ConfigKeys.QUEUE);
    }
}
