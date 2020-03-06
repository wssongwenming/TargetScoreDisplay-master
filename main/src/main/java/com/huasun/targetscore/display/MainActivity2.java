package com.huasun.targetscore.display;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huasun.core.activities.ProxyActivity;
import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.huasun.core.delegates.LatteDelegate;
import com.huasun.core.ui.launcher.ILauncherListener;
import com.huasun.core.ui.launcher.OnLauncherFinishTag;
import com.huasun.core.util.ActivityManager;
import com.huasun.core.util.DataCleanManager;
import com.huasun.display.launcher.LauncherDelegate;
import com.huasun.display.main.mark.IMarkAttachListener;
import com.huasun.display.main.mark.MarkDelegate;
import com.huasun.display.main.mark.view.MarkDisplay;
import com.huasun.display.sign.ISignListener;
import com.huasun.display.sign.SignInBottomDelegate;
import com.huasun.display.sign.SignInByFace.SignInByFaceRecDelegate;
import com.huasun.display.sign.SignInByPassword.SignInByPassDelegate;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MainActivity2 extends ProxyActivity implements ISignListener,ILauncherListener,IMarkAttachListener{

    private ConnectionFactory factory = new ConnectionFactory();// 声明ConnectionFactory对象
    Thread subscribeThread;
    private MarkDelegate markDelegate;
    //Activity是否已经收到了服务器端就绪的命令，如果Activity收到了该命令并根据传入的参数(0:密码登陆，1：脸部识别登陆,2:等候中)进入相应界面
    private String server="192.168.1.3";
    private String exchange_name = "server-to-other-exchange";
    private String exchange_type="topic";
    private String commandQueueName="";
    private String commandRoutingKey="";
    private String markDataQueueName="";
    private String markDataRoutingKey="";
    private int port=5672;
    private String username="client";
    private String password="client";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
        Latte.getConfigurator().withActivity(this);
        String mac=getMac(this);
        commandQueueName="server-to-display-commandqueue-"+mac;
        commandRoutingKey="server-to-display-command-routing-key-"+mac;
        markDataQueueName="server-to-display-markdataqueue-"+mac;
        markDataRoutingKey="server-to-display-markdata-routing-key-"+mac;
        //连接设置
        setupConnectionFactory();
        //用于从线程中获取数据，更新ui
        final Handler incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                final JSONObject command= JSON.parseObject( message);
                int dataType=command.getInteger("dataType");
                Latte.getConfigurator().withCommand(dataType);
                if(dataType==4){//完毕退出
                    DataCleanManager.cleanApplicationData((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY));
                    ActivityManager.getInstance().finishActivitys();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }else if(dataType==0){
                    startWithPop(new LauncherDelegate());
                }
                else if(dataType==1){
                    Toast.makeText((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY),"ok",Toast.LENGTH_LONG).show();
                    startWithPop(SignInByPassDelegate.newInstance(message));
                }else if(dataType==2){
                    startWithPop(SignInByFaceRecDelegate.newInstance(message));
                }else if(dataType==3){//不会直接走，
                    startWithPop(MarkDelegate.newInstance(message));
                }else if(dataType==5){
                    if(markDelegate!=null){
                        MarkDisplay markDisplay=markDelegate.getMarkDisplay();
                        if(markDisplay!=null) {
                            markDisplay.setMarkJson(message);
                            markDelegate.mRefreshHandler.initData(message);
                        }
                    }
                }
            }
        };
        //开启消费者线程
        subscribe(incomingMessageHandler);
    }
    @Override
    public LatteDelegate setRootDelegate() {
        return new LauncherDelegate();
    }
    @Override
    public void onSignInSuccess(int index, String command) {
        startWithPop(new LauncherDelegate());
    }
    @Override
    public void onSignUpSuccess() {
        Toast.makeText(this,"登陆成功",Toast.LENGTH_LONG).show();
    }
    @Override
    public void onSignInError(String msg){
    }
    @Override
    public void onSignUpError(String msg) {
    }
    @Override
    public void onSignInFailure(String msg) {
    }
    @Override
    public void onSignUpFailure(String msg) {
    }
     @Override
    public void onLauncherFinish(OnLauncherFinishTag tag) {
        switch (tag){
            case SIGNIN_BY_PASS:
                Toast.makeText(this,"启动结束，用户将以密码登陆",Toast.LENGTH_LONG).show();
                start(new SignInBottomDelegate());
                break;
            case SIGNIN_BY_FACE:
                Toast.makeText(this,"启动结束，用户将以人脸识别方式登陆",Toast.LENGTH_LONG).show();
                start(new SignInBottomDelegate());
                break;
        }
    }

    @Override
    public int getStatus() {
        return Latte.getConfiguration(ConfigKeys.COMMAND);
    }

    @Override
    public void setMarkDelegate(MarkDelegate markDelegate) {
        this.markDelegate=markDelegate;
    }

    @Override
    public MarkDelegate getMarkDelegate() {
        return markDelegate;
    }


    /**
     * 连接设置
     */
    private void setupConnectionFactory() {
        factory.setHost(server);
        factory.setPort(5672);
        factory.setUsername("client");
        factory.setPassword("client");
    }

    /**
     * 消费者线程
     */
    void subscribe(final Handler handler) {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //使用之前的设置，建立连接
                        Connection connection = factory.newConnection();
                        //创建一个通道
                        Channel channel = connection.createChannel();
                        //一次只发送一个，处理完成一个再获取下一个
                        channel.basicQos(1);
                        //exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete,Map<String, Object> arguments) throws IOException;
                        channel.exchangeDeclare(exchange_name, exchange_type, true, false, null);
                        //queueDeclare (String queue , boolean durable , boolean exclusive , boolean autoDelete , Map arguments)
                        AMQP.Queue.DeclareOk q = channel.queueDeclare(commandQueueName,false,false,true,null);
                        //queueDeclare (String queue , boolean durable , boolean exclusive , boolean autoDelete , Map arguments)
                        AMQP.Queue.DeclareOk p = channel.queueDeclare(markDataQueueName,false,false,true,null);
                        //将队列绑定到消息交换机exchange上
                        //                  queue         exchange              routingKey路由关键字，exchange根据这个关键字进行消息投递。
                        channel.queueBind(q.getQueue(), exchange_name, commandRoutingKey);
                        channel.queueBind(q.getQueue(), exchange_name, markDataRoutingKey);

                        //创建消费者
                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume(q.getQueue(), true, consumer);
                        channel.basicConsume(p.getQueue(), true, consumer);

                        while (true) {
                            //wait for the next message delivery and return it.
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            String message = new String(delivery.getBody());

                            Log.d("", "[r] " + message);

                            //从message池中获取msg对象更高效
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("msg", message);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e1) {
                        Log.d("", "Connection broken: " + e1.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });
        subscribeThread.start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscribeThread.interrupt();
    }

    /**
     * Android 6.0 之前（不包括6.0）获取mac地址
     * 必须的权限 <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     * @param context * @return
     */
    public static String getMacDefault(Context context) {
        String mac = "";
        if (context == null) {
            return mac;
        }
        WifiManager wifi = (WifiManager)Latte.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }
    /**
     * Android 6.0-Android 7.0 获取mac地址
     */
    public static String getMacAddress() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat/sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            while (null != str) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();//去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }

        return macSerial;
    }

    /**
     * Android 7.0之后获取Mac地址
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET"></uses-permission>
     * @return
     */
    public static String getMacFromHardware() {
        try {
            ArrayList<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equals("wlan0"))
                    continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) return "";
                StringBuilder res1 = new StringBuilder();
                for (Byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (!TextUtils.isEmpty(res1)) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public String getMac( Context context){
        String mac= "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacAddress();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        return mac;
    }
}
/*
将command和markdata合二为一通过消息类型判断逻辑走向。然后运用同一个channel通道向server// 声明一个队列 -// queue 队列名称
        // durable 为true时server重启队列不会消失 (是否持久化)
        // exclusive 队列是否是独占的，如果为true只能被一个connection使用，其他连接建立时会抛出异常
        // autoDelete 当没有任何消费者使用时，自动删除该队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);*/
