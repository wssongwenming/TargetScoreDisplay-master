package com.huasun.targetscore.display;

import android.app.Application;

import com.huasun.core.app.Latte;
import com.huasun.core.net.interceptors.DebugInterceptor;
import com.huasun.core.rabbitmq.MessageConsumer;
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
    private String server="192.168.1.3";
    private String exchange_name = "server-to-display-exchange";
    private String exchange_type="topic";
    private int port=5672;
    private String username="client";
    private String password="client";
    @Override
    public void onCreate() {
        super.onCreate();
        Latte.init(this)
                .withIcon(new FontAwesomeModule())
                .withIcon(new FontBcModule())
                .withCommand(0)
                .withMessageConsumer(new MessageConsumer(server,exchange_name,exchange_type,port,username,password))
                .withApiHost("http://127.0.0.1")
                .withInterceptor(new DebugInterceptor("index",R.raw.test))
                .configure();
        DatabaseManager.getInstance().init(this);
        //异常处理功能模块，如发生exception不会退出w
        SecyrityCrash.install();
        ////异常处理功能模块，如发生exception会提示后退出
        //CrashHandler crashHandler = CrashHandler.getInstance();
        //crashHandler.init(getApplicationContext());
    }

}
