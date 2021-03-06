package com.huasun.targetscore.display;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huasun.core.activities.ProxyActivity;
import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.huasun.core.delegates.LatteDelegate;

import com.huasun.core.rabbitmq.MessageConsumer;

import com.huasun.core.ui.launcher.ILauncherListener;
import com.huasun.core.ui.launcher.OnLauncherFinishTag;
import com.huasun.core.util.ActivityManager;
import com.huasun.core.util.Config;
import com.huasun.core.util.DataCleanManager;
import com.huasun.core.util.SoundPoolUtil;
import com.huasun.core.util.asyncplayer.AsyncPlayer;
import com.huasun.display.entity.MessagetoServer;
import com.huasun.display.launcher.LauncherDelegate;
import com.huasun.display.main.mark.IMarkAttachListener;
import com.huasun.display.main.mark.MarkDelegate;
import com.huasun.display.main.mark.view.MarkDisplay;
import com.huasun.display.sign.ISignListener;
import com.huasun.display.sign.SignInBottomDelegate;
import com.huasun.display.sign.SignInByFace.SignInByFaceRecDelegate;
import com.huasun.display.sign.SignInByPassword.SignInByPassDelegate;

import com.huasun.display.wait.WaitDelegate;
import com.huasun.targetscore.rabbitmq.RabbitMQConsumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends ProxyActivity implements ISignListener,ILauncherListener,IMarkAttachListener {
    DataHandler dataHandler = DataHandler.getInstance();
//
    public boolean isShooting=false;
    //资源列表,用于播放声音
    private Map<String, Integer> map ;
    private List<Integer> zh ;
    private MarkDelegate markDelegate;
    //Activity是否已经收到了服务器端就绪的命令，如果Activity收到了该命令并根据传入的参数(0:密码登陆，1：脸部识别登陆,2:等候中)进入相应界面
    private RabbitMQConsumer mConsumer;
    private String server = Config.serverIp;
    private String exchange_name = "server-to-display-exchange";
    private String exchange_type = "topic";
    private int port = 5672;
    private String username = "client";
    private String password = "client";

    private String queueName = "";
    private String routingKey = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Latte.getConfigurator().withActivity(this);
        dataHandler.setMainActivity(this);
        Latte.getConfigurator().withIsShooting(false);
        Latte.getConfigurator().withConnectRabbit(false);

//        高版本不能在主线程中启动HTTP
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        String mac = getMac(this);
        queueName = "server-to-display-queue-" + mac;
        routingKey = "server-to-display-routing-key-" + mac;

        //开始消息队列
//        mConsumer = Latte.getConfiguration(ConfigKeys.MESSAGECONSUMER);
        mConsumer=new RabbitMQConsumer(server,exchange_name,port,username,password,exchange_type,queueName,routingKey,this);
        new consumerconnect().execute();

        mConsumer.setOnReceiveMessageHandler(new com.huasun.targetscore.rabbitmq.MessageConsumer.OnReceiveMessageHandler() {
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void onReceiveMessage(byte[] text) {
                String message = "";
                try {
                    message = new String(text, "UTF8");
                    final JSONObject command = JSON.parseObject(message);
                    int dataType = command.getInteger("dataType");
                    Latte.getConfigurator().withCommand(dataType);
                    if (dataType == DataType.EXIT.getCode()) {//完毕退出
                        DataCleanManager.cleanApplicationData((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY));
                        ActivityManager.getInstance().finishActivitys();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    } else if (dataType == DataType.LAUNCH.getCode()) {
                        startWithPop(new LauncherDelegate());
                    } else if (dataType == DataType.SIGNINBYPASS.getCode()) {
                        //Toast.makeText((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY), "ok", Toast.LENGTH_LONG).show();
                        dataHandler.clearData();
                        startWithPop(SignInByPassDelegate.newInstance(message));
                    } else if (dataType == DataType.SIGNINBYFACE.getCode()) {

                        startWithPop(SignInByFaceRecDelegate.newInstance(message));
                    } else if (dataType == DataType.STARTSHOOTING.getCode()) {//不会直接走，
                        Latte.getConfigurator().withIsShooting(true);
                        startWithPop(MarkDelegate.newInstance(message));
                    } else if (dataType == DataType.MARK_DATA.getCode()) {
                        if (markDelegate != null) {

                            MarkDisplay markDisplay = markDelegate.getMarkDisplay();
                            if (markDisplay != null) {
                                markDisplay.setMarkJson(message);
                                markDelegate.mRefreshHandler.initData(message);
                                if(message!=null&&!message.isEmpty()) {
                                    final JSONArray increasedRingNumbersOffsetArray = JSON.parseObject(message).getJSONArray("increasedRingNumbersAndOffset");
                                    int size=increasedRingNumbersOffsetArray.size();
                                    final List<String> ringNumberlists = new ArrayList<>() ;
                                    for (int i=0;i<size;i++){//increasedRingNumbersOffsetArray的数据是：ringnumber，offset，ringnumber，offset
                                        System.out.print("aaaaaaaaaa"+increasedRingNumbersOffsetArray);
                                        String ringnumber= String.valueOf((increasedRingNumbersOffsetArray.getInteger(i)));
                                        i++;
                                        String offset= String.valueOf((increasedRingNumbersOffsetArray.getString(i)));


                                        dataHandler.addData(ringnumber,offset);
                                        ringNumberlists.add(ringnumber);
                                    }
                                    if(size>0){
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
//                                                List<String> lists = new ArrayList<>() ;
//                                                lists.add("cat");
                                                //播放6次猫叫和鸟叫(组合播放)
//                                                zh = SoundPoolUtil.getInstance().play(ringNumberlists, 0) ;
                                            }
                                        }).start();
//                                        new Thread() {
//                                            private List<String> ringNumber = new ArrayList<>() ;
//                                            public Thread setRingNumberList(List<String> stringList) {
//                                                this.ringNumber = stringList;
//                                                return this;
//                                            }
//
//                                            @Override
//                                            public void run() {
//                                                zh = SoundPoolUtil.getInstance().play(ringNumber, 6) ;
//                                            }
//                                        }.start();
                                    }
                                }



                            }
                        }
                    } else if (dataType == DataType.DEVICESTATUS.getCode()) {//服务器端发出了查询display端状态
                        JSONObject data=command.getJSONObject("data");
                        int deviceId=data.getIntValue("deviceId");
                        int deviceGroupIndex=data.getIntValue("deviceGroupIndex");

                        String exchangeName="display-to-server-exchange";
                        String routingKey="display-to-server-routing-key";
                        new send().execute(deviceId+"",deviceGroupIndex+"",exchangeName,routingKey);
/*
                        public class Command {
                            private int code;
                            private String message;
                            private int dataType;
                            private Object data;
                              注解：
                             data {
                                private int deviceType;//靶机，采集，显靶分别为0，1，2
                                private int deviceGroupIndex;
                                private int deviceIndex;
                                private int deviceId;*/

                        //TODO 回复Display状态

                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        init();
        //启动一个线程监控当前rabbitmq的状态，实时修改消息队列连接状况图标，由于在activity中可以找到fragment中的元素，
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                            boolean connect_to_rabbitmq=Latte.getConfiguration(ConfigKeys.CONNECT_RABBIT);
                            if(connect_to_rabbitmq)
                            {
                                Latte.getHandler().post(new Runnable() {
                                    @Override
                                    public void run() {

                                        AppCompatImageView imageView=findViewById(R.id.tv_ring_icon);
                                        imageView.setImageResource(R.drawable.ring_connect);
                                    }
                                });
                            }
                            else
                            {
                                Latte.getHandler().post(new Runnable() {
                                    @Override
                                    public void run() {

                                        AppCompatImageView imageView=findViewById(R.id.tv_ring_icon);
                                        imageView.setImageResource(R.drawable.ring_disconnect);
                                    }
                                });
                            }

                        Thread.sleep(10);
                    } catch (InterruptedException ignore) {}
                }
            }

        }).start();


    }

//    @Override
//    protected void onStart() {
//        super.onStart();
////      Resources resources=this.getResources();;//获取本地资源
////      RelativeLayout relativeLayout=findViewById(R.id.layout_launch);
////      relativeLayout.setBackground(resources.getDrawable(R.drawable.background));
//    }

    public void refresh(){
        Resources resources=this.getResources();;//获取本地资源
        RelativeLayout relativeLayout=findViewById(R.id.layout_launch);
        relativeLayout.setBackground(resources.getDrawable(R.drawable.background));
    }

    private void init() {
        map = new HashMap<>() ;
        map.put("0", R.raw.zero);
        map.put("1", R.raw.one) ;
        map.put("2", R.raw.two) ;
        map.put("3", R.raw.three) ;
        map.put("4", R.raw.four) ;
        map.put("5", R.raw.five) ;
        map.put("6", R.raw.six) ;
        map.put("7", R.raw.seven) ;
        map.put("8", R.raw.eight) ;
        map.put("9", R.raw.nine) ;
        map.put("10", R.raw.ten) ;
        map.put("u",R.raw.u);
        map.put("d",R.raw.d);
        map.put("lu",R.raw.lu);
        map.put("ru",R.raw.ru);
        map.put("ld",R.raw.ld);
        map.put("rd",R.raw.rd);
        map.put("l",R.raw.l);
        map.put("r",R.raw.r);
        SoundPoolUtil.getInstance().loadR(this, map);
    }
    @Override
    public LatteDelegate setRootDelegate() {
        return new LauncherDelegate();
    }

    @Override
    public void onSignInSuccess(int index, String command) {
        startWithPop(new WaitDelegate());
    }

    @Override
    public void onSignUpSuccess() {
        Toast.makeText(this, "登陆成功", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSignInError(String msg) {
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
        switch (tag) {
            case SIGNIN_BY_PASS:
                Toast.makeText(this, "启动结束，用户将以密码登陆", Toast.LENGTH_LONG).show();
                start(new SignInBottomDelegate());
                break;
            case SIGNIN_BY_FACE:
                Toast.makeText(this, "启动结束，用户将以人脸识别方式登陆", Toast.LENGTH_LONG).show();
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
        this.markDelegate = markDelegate;
    }

    @Override
    public MarkDelegate getMarkDelegate() {
        return markDelegate;
    }

    private class consumerconnect extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... Message) {
            try {
//             mConsumer.connectToRabbitMQ(queueName,routingKey);
               mConsumer.connectToRabbitMQ();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }

    protected void onResume() {
        super.onResume();
//        new consumerconnect().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConsumer.Dispose();//此处需要认证考虑
        Toast.makeText(this, "正在退出", Toast.LENGTH_LONG).show();
    }
    /**
     * Android 6.0 之前（不包括6.0）获取mac地址
     * 必须的权限 <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     *
     * @param context * @return
     */
    public static String getMacDefault(Context context) {
        String mac = "";
        if (context == null) {
            return mac;
        }
        WifiManager wifi = (WifiManager) Latte.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
     *
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

    public String getMac(Context context) {
        String mac = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacAddress();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        mac = mac.replace(":", "").toLowerCase();
        return mac;
    }

    private enum DataType {
        LAUNCH(0),
        SIGNINBYPASS(1),
        SIGNINBYFACE(2),//2
        STARTSHOOTING(3),//3
        EXIT(4),//4
        MARK_DATA(5),//5
        DEVICESTATUS(6);//6
        private int code;

        private DataType(int _code) {
            this.code = _code;
        }

        private int getCode() {
            return code;
        }

        @Override

        public String toString() {

            return String.valueOf(this.code);

        }

    }

    private class send extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... Message) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(server);
                factory.setUsername(username);
                factory.setPassword(password);
                factory.setPort(port);
                String deviceId=Message[0];
                String deviceGroupIndex=Message[1];
                String EXCHANGENAME=Message[2];
                String ROUTINGKEY=Message[3];
                MessagetoServer message = new MessagetoServer();
                message.setCode(1);//code=1表示返回ｓｅｒｖｅｒ设备状态
                message.setDeviceId(Integer.parseInt(deviceId));
                message.setTarget_index(Integer.parseInt(deviceGroupIndex));
                message.setDevice_status(0);//0，正常，1：异常，只要能返回就是正常
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.basicPublish(EXCHANGENAME, ROUTINGKEY, null, JSONObject.toJSONString(message).getBytes());
                channel.close();
                connection.close();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }
    //启动一个线程监听全局队列并播放声音靶环和偏移方向队列，
    class getRingNumbersOffsetThread extends Thread{
        @Override
        public void run() {
            super.run();
            //干你需要做的操作
        }
    }
}



/*    int LAUNCH_COMMAND = 0;

    int SIGNINBYPASS_COMMAND = 1;

    int SIGNINBYFACE_COMMAND=2;

    int STARTSHOOTING_COMMAND= 3;

    int EXIT_COMMAND = 4;

    int MARK_DATA=5;

    int DEVICESTATUS=6;
将command和markdata合二为一通过消息类型判断逻辑走向。然后运用同一个channel通道向server// 声明一个队列 -// queue 队列名称
        // durable 为true时server重启队列不会消失 (是否持久化)
        // exclusive 队列是否是独占的，如果为true只能被一个connection使用，其他连接建立时会抛出异常
        // autoDelete 当没有任何消费者使用时，自动删除该队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);*/
