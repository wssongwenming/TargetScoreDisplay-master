package com.huasun.targetscore.display;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bcsb.rabbitmq.entity.Command;
import com.huasun.core.activities.ProxyActivity;
import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.huasun.core.delegates.LatteDelegate;
import com.huasun.core.ui.launcher.ILauncherListener;
import com.huasun.core.ui.launcher.OnLauncherFinishTag;
import com.huasun.core.util.ActivityManager;
import com.huasun.core.util.DataCleanManager;
import com.huasun.display.database.UserProfile;
import com.huasun.display.launcher.LauncherDelegate;
import com.huasun.display.main.mark.IMarkAttachListener;
import com.huasun.display.main.mark.MarkDelegate;
import com.huasun.display.sign.ISignListener;
import com.huasun.display.sign.SignInBottomDelegate;
import com.huasun.display.sign.SignInByFace.SignInByFaceRecDelegate;
import com.huasun.display.sign.SignInByPassword.SignInByPassDelegate;
import com.huasun.targetscore.rabbitmq.MessageConsumer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends ProxyActivity implements ISignListener,ILauncherListener,IMarkAttachListener{

    AppCompatTextView mBasicSituation=null;
    LinearLayoutCompat mDataContainer=null;
    private MarkDelegate markDelegate;
    //Activity是否已经收到了服务器端就绪的命令，如果Activity收到了该命令并根据传入的参数(0:密码登陆，1：脸部识别登陆,2:等候中)进入相应界面
    private int currentcommand=Latte.getConfiguration(ConfigKeys.COMMAND);
    private MessageConsumer mConsumer;
    private String server="192.168.1.3";
    //private String queue_name = "signin-queue";
    private String exchange_name = "bcsb-exchange";
    private String exchange_type="topic";

    private MarkDelegate latestmarkDelegate;

    private String commandQueueName="";
    private String commandRoutingKey="";

    private String markDataQueueName="";
    private String markDataRoutingKey="";
    private int port=5672;
    private String username="client";
    private String password="client";
    private SignInBottomDelegate signInBottomDelegate=null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
        Latte.getConfigurator().withActivity(this);
        String ip=getIpAddress();
        String number=getLastIP(ip);//去ip地址最后部分为靶位编号，在开始时绑定mac地址和ip，让ip为1，2，3
        commandQueueName="command_queue_"+number;
        commandRoutingKey="command_routing_key_"+number;
        markDataQueueName="markData_queue_"+number;
        markDataRoutingKey="markData_routing_key_"+number;
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
                    markDelegate.getMarkDisplay().setMarkJson(markJson);
                    markDelegate.mRefreshHandler.initData(markJson);
                }

/*              MarkDelegate currentDelegate=(MarkDelegate) signInBottomDelegate.getTopChildFragment();
                currentDelegate.getMarkDisplay().setMarkJson(markJson);//
                currentDelegate.mRefreshHandler.initData(markJson);//初始化markdelegate的recyclerview的数据
                latestmarkDelegate=currentDelegate;*/
            }
        });
        mConsumer.setOnReceiveCommandMessageHandler(new MessageConsumer.OnReceiveCommandMessageHandler() {
            public void onReceiveMessage(byte[] message) {
                Log.d("command", "onReceiveMessage: command");
                String commandJson = new String(message);//将收到的数据还原为json, 发送方发送时为json
                final JSONObject command= JSON.parseObject(commandJson);
                int newcommand=command.getInteger("index");
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
                }else if(newcommand==3){
                    startWithPop(MarkDelegate.newInstance(commandJson));
                }

                }
        });
    }
    @Override
    public LatteDelegate setRootDelegate() {
        return new LauncherDelegate();
        //this.signInBottomDelegate=new SignInBottomDelegate();
        //return this.signInBottomDelegate;
        //return new MarkDelegate();
        //return new MainDelegate();
        //刚开始为等待下命令状态

        //return SignInBottomDelegate.newInstance(Command.waiting.getIndex());
    }

    @Override
    public void onSignInSuccess(int index, String command) {
        Toast.makeText(this,"登陆成功",Toast.LENGTH_LONG).show();
        startWithPop(MarkDelegate.newInstance(command));
        /*signInBottomDelegate.showHideFragment(signInBottomDelegate.getITEM_DELEGATES().get(index), signInBottomDelegate.getITEM_DELEGATES().get(currentcommand));
        currentcommand = index;
        MarkDelegate currentDelegate=(MarkDelegate) signInBottomDelegate.getTopChildFragment();
        currentDelegate.initBasicData(userProfile);
        currentDelegate.getMarkDisplay().setMarkJson("");
        currentDelegate.mRefreshHandler.initData("");
        //startWithPop(new BcsbBottomDelegate());*/
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
    protected void onPause() {
        super.onPause();
        mConsumer.Dispose();//此处需要认证考虑
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

}
