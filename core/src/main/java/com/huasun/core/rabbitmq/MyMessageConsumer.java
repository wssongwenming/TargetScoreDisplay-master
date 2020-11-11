package com.huasun.core.rabbitmq;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

/**
 * author:songwenming
 * Date:2020/10/4
 * Description:
 */
public class MyMessageConsumer {

    public String mServer;//server地址
    public String mExchange;//exchange名
    public int mPort;
    public String mUsername;
    public String mPassWord;

    protected Channel mModel = null;//channel
    protected Connection mConnection;

    protected boolean Running ;

    protected String MyExchangeType ;
    /**
     * @param server       The server address
     * @param exchange     The named exchange
     * @param exchangeType The exchange type name
     * @param port
     * @param username
     * @param password
     */
    public MyMessageConsumer(String server, String exchange, String exchangeType, int port, String username, String password) {
        mServer = server;
        mExchange = exchange;
        MyExchangeType = exchangeType;
        mPort=port;
        mUsername=username;
        mPassWord=password;
    }
    //The Queue name for this consumer
    private String mQueue;
    private MyConsumer MySubscription;
    //last message to post back
    private byte[] mLastMessage;

    // An interface to be implemented by an object that is interested in messages(listener)
    public interface OnReceiveMessageHandler{

        public void onReceiveMessage(byte[] message);
    };

    //A reference to the listener, we can only have one at a time(for now)
    private OnReceiveMessageHandler mOnReceiveMessageHandler;

    /**
     *
     * Set the callback for received messages
     * @param handler The callback
     */
    public void setOnReceiveMessageHandler(OnReceiveMessageHandler handler){
        if(MySubscription!=null)
        {
            MySubscription.setOnReceiveMessageHandler((MyConsumer.OnReceiveMessageHandler) handler);
        }
//        mOnReceiveMessageHandler = handler;
    };

    private Handler mMessageHandler = new Handler();
    private Handler mConsumeHandler = new Handler();

    // Create runnable for posting back to main thread
    final Runnable mReturnMessage = new Runnable() {
        public void run() {
            mOnReceiveMessageHandler.onReceiveMessage(mLastMessage);
        }
    };

//    final Runnable mConsumeRunner = new Runnable() {
//        public void run() {
//            Consume();
//        }
//    };


    /**
     * Add a binding between this consumers Queue and the Exchange with routingKey
     * @param routingKey the binding key eg GOOG
     */
    public void AddBindingQueue(String queueName,String exchangName,String routingKey)
    {
        try {
            mModel.queueBind(queueName, exchangName, routingKey);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * Remove binding between this consumers Queue and the Exchange with routingKey
     * @param routingKey the binding key eg GOOG
     */
    public void RemoveBinding(String routingKey)
    {
        try {
            mModel.queueUnbind(mQueue, mExchange, routingKey);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public void Dispose()
    {
        Running  = false;

        try {
            if (mModel != null)
                mModel.abort();
            if (mConnection!=null)
                mConnection.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean connectToRabbitMQ()
    {
      /*        if(mModel!= null && mModel.isOpen() )//already declared
            return true;*/
        try
        {
            if(mModel!= null && mModel.isOpen() )//already declared
            {
                mModel.close();

            }

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mServer);
            connectionFactory.setUsername(mUsername);
            connectionFactory.setPassword(mPassWord);
            connectionFactory.setPort(mPort);
            mConnection = connectionFactory.newConnection();
            //测试
//            mConnection.addShutdownListener();
            //创建一个通道
            mModel = mConnection.createChannel();
            //只有在channel.basicQos被使用的时候channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false)才起到作用。
            mModel.basicQos(1);
            //创建一个的交换器
            //exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete,
            //                                       Map<String, Object> arguments) throws IOException;
            mModel.exchangeDeclare(mExchange, MyExchangeType, false, true, null);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Latte.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY),"未能正常连接消息队列，请检查设置后，并重新启动App",Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }
    }

    public boolean connectToRabbitMQ(String queueName,String exchangeName,String routingKey)
    {
        if(connectToRabbitMQ())
        {

            try {
                //queueDeclare (String queue , boolean durable , boolean exclusive , boolean autoDelete , Map arguments)
                mQueue = mModel.queueDeclare(queueName,false,false,true,null).getQueue();
                MySubscription = new MyConsumer(mModel);
                mModel.basicConsume(mQueue, false, MySubscription);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            if (MyExchangeType == "topic"||MyExchangeType == "direct")
                AddBindingQueue(queueName,exchangeName,routingKey);

            Running = true;
            return true;
        }
        return false;
    }

}
