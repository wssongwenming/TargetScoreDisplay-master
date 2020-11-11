package com.huasun.core.delegates.bottom;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.huasun.core.R;
import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.huasun.core.delegates.LatteDelegate;
import com.huasun.core.rabbitmq.MessageConsumer;
import com.huasun.core.rabbitmq.RabbitMQConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;


/**
 * author:songwenming
 * Date:2019/9/25
 * Description:
 */
public abstract class BottomItemDelegate extends LatteDelegate {
    // 再点一次退出程序时间设置
    private static final long WAIT_TIME = 2000L;
    private long TOUCH_TIME = 0;

    @Override
    public boolean onBackPressedSupport() {
        if (System.currentTimeMillis() - TOUCH_TIME < WAIT_TIME) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    ((RabbitMQConsumer)Latte.getConfiguration(ConfigKeys.MESSAGECONSUMER)).Dispose();//关闭消息队列
//                }
//            }).start();
           // ((MessageConsumer)Latte.getConfiguration(ConfigKeys.MESSAGECONSUMER)).Dispose();//关闭消息队列
            _mActivity.finish();
            System.exit(0);
        } else {
            TOUCH_TIME = System.currentTimeMillis();
            Toast.makeText(_mActivity, "再单击一次退出" + Latte.getApplicationContext().getString(R.string.app_name), Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
