package com.huasun.targetscore.display;

import android.app.Application;
import android.util.Log;

import com.bcsb.rabbitmq.entity.Command;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.huasun.core.app.Latte;
import com.huasun.core.net.interceptors.DebugInterceptor;
import com.huasun.display.database.DatabaseManager;
import com.huasun.display.icon.FontBcModule;
import com.huasun.targetscore.exception.SecyrityCrash;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

/**
 * author:songwenming
 * Date:2019/9/21
 * Description:
 */
public class MainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Latte.init(this)
                .withIcon(new FontAwesomeModule())
                .withIcon(new FontBcModule())
                .withCommand(0)
                .withApiHost("http://127.0.0.1")
                .withInterceptor(new DebugInterceptor("index",R.raw.test))
                .configure();
        DatabaseManager.getInstance().init(this);
        //异常处理功能模块，如发生exception不会退出
        SecyrityCrash.install();
        ////异常处理功能模块，如发生exception会提示后退出
        //CrashHandler crashHandler = CrashHandler.getInstance();
        //crashHandler.init(getApplicationContext());
    }
}
