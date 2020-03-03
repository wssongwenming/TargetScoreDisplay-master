package com.huasun.targetscore.display;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import com.huasun.targetscore.rabbitmq.MessageConsumer;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity1 extends ProxyActivity implements ISignListener,ILauncherListener,IMarkAttachListener{

    private ConnectionFactory factory = new ConnectionFactory();// 声明ConnectionFactory对象
    private BlockingDeque<String> queue = new LinkedBlockingDeque<>();


    private MarkDelegate markDelegate;
    //Activity是否已经收到了服务器端就绪的命令，如果Activity收到了该命令并根据传入的参数(0:密码登陆，1：脸部识别登陆,2:等候中)进入相应界面
    private int currentcommand=Latte.getConfiguration(ConfigKeys.COMMAND);
    private MessageConsumer mConsumer;
    private String server="192.168.1.3";

    //private String exchange_name = "bcsb-exchange";

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
        String ip=getIpAddress();
        String number=getLastIP(ip);//去ip地址最后部分为靶位编号，在开始时绑定mac地址和ip，让ip为1，2，3
        String mac=getMac(this);

        commandQueueName="server-to-display-commandqueue-"+mac;
        commandRoutingKey="server-to-display-command-routing-key-"+mac;
        markDataQueueName="server-to-display-markdataqueue-"+mac;
        markDataRoutingKey="server-to-display-markdata-routing-key-"+mac;

        //开始消息队列
        // Create the consumer
        mConsumer = new MessageConsumer(server, exchange_name, exchange_type,port,username,password);
        new consumerconnect().execute();
        mConsumer.setOnReceiveMarkDataHandler(new MessageConsumer.OnReceiveMarkDataHandler() {
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void onReceiveMessage(byte[] message) {
                String markJson = new String(message);//将收到的数据还原为json, 发送方发送时为json
                if(markDelegate!=null){
                    MarkDisplay markDisplay=markDelegate.getMarkDisplay();
                    if(markDisplay!=null) {
                        markDisplay.setMarkJson(markJson);
                        markDelegate.mRefreshHandler.initData(markJson);
                    }
                }
            }
        });
        mConsumer.setOnReceiveCommandMessageHandler(new MessageConsumer.OnReceiveCommandMessageHandler() {
            public void onReceiveMessage(byte[] message) {
                Log.d("command", "onReceiveMessage: command");
                String commandJson = new String(message);//将收到的数据还原为json, 发送方发送时为json
                final JSONObject command= JSON.parseObject(commandJson);
                int newcommand=command.getInteger("dataType");
                Latte.getConfigurator().withCommand(newcommand);
                if(newcommand==4){//完毕退出
                    DataCleanManager.cleanApplicationData((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY));
                    ActivityManager.getInstance().finishActivitys();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }else if(newcommand==0){
                    startWithPop(new LauncherDelegate());
                }
                else if(newcommand==1){
                    Toast.makeText((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY),"ok",Toast.LENGTH_LONG).show();
                    startWithPop(SignInByPassDelegate.newInstance(commandJson));
                }else if(newcommand==2){
                    startWithPop(SignInByFaceRecDelegate.newInstance(commandJson));
                }else if(newcommand==3){//不会直接走，
                    startWithPop(MarkDelegate.newInstance(commandJson));
                }

                }
        });
    }
    @Override
    public LatteDelegate setRootDelegate() {
        return new LauncherDelegate();

    }

    @Override
    public void onSignInSuccess(int index, String command) {
        //Toast.makeText(this,"登陆成功",Toast.LENGTH_LONG).show();
        //startWithPop(MarkDelegate.newInstance(command));
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

    //消息队列相关函数
    private  class consumerconnect extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... Message) {
            try {
                // Connect to broker
                mConsumer.connectToCommandRabbitMQ(commandQueueName,exchange_name,commandRoutingKey);
                mConsumer.connectToMarkDataRabbitMQ(markDataQueueName,exchange_name,markDataRoutingKey);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        new consumerconnect().execute();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConsumer.Dispose();//此处需要认证考虑
        //publishMessage(getException());
    }
    private String getIpAddress(){
        try{
            for(Enumeration<NetworkInterface> enNetI=NetworkInterface.getNetworkInterfaces();enNetI.hasMoreElements();){
                NetworkInterface netI=enNetI.nextElement();
                for(Enumeration<InetAddress>enumIpAdress =netI.getInetAddresses();enumIpAdress.hasMoreElements();){
                    InetAddress inetAddress=enumIpAdress.nextElement();
                    if(inetAddress instanceof Inet4Address&&!inetAddress.isLoopbackAddress()){
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }catch (SocketException e)
        {
            Latte.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY),"网络连接有问题，请检查设置后，并重新启动App",Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace();
        }
        return "";
    }

    private String getLastIP(String ip){
        int index=ip.lastIndexOf(".");
        String lastIP=ip.substring(index+1,ip.length());
        return lastIP;
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
