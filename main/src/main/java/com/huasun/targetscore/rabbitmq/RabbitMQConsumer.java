package com.huasun.targetscore.rabbitmq;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.huasun.display.launcher.LauncherDelegate;
import com.huasun.targetscore.display.MainActivity;
import com.huasun.targetscore.display.R;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * author:songwenming
 * Date:2020/10/5
 * Description:
 */
public class RabbitMQConsumer {
    MainActivity activity;
    private String amqpHost="";
    private Integer port=5672;
    private String username="";
    private String password="";
    protected String exchangeType="" ;
    private String amqpExchangeName="";
    private String queueName="";
    private String routingKey="";
    private Integer retryInterval=500;
    protected Channel mModel = null;//channel
    protected Connection mConnection;
    // connection to AMQP server,
    private  Connection s_connection = null;

    //??????可能引发问题

    private  Handler myHandler=new Handler();

    // AMQP server should consider messages acknowledged once delivered if _autoAck is true
    private boolean s_autoAck = false;

    private ExecutorService executorService;
    private  DisconnectHandler disconnectHandler;
    private  BlockedConnectionHandler blockedConnectionHandler;
    private  final Logger s_logger = Logger.getLogger(RabbitMQConsumer.class);

    //last message to post back
    private byte[] mLastMessage;

    // An interface to be implemented by an object that is interested in messages(listener)
    public interface OnReceiveMessageHandler{

        public void onReceiveMessage(byte[] message);
    };

    //A reference to the listener, we can only have one at a time(for now)
    private MessageConsumer.OnReceiveMessageHandler mOnReceiveMessageHandler;

    /**
     *
     * Set the callback for received messages
     * @param handler The callback
     */
    public void setOnReceiveMessageHandler(MessageConsumer.OnReceiveMessageHandler handler){
        mOnReceiveMessageHandler = handler;
    };

    private Handler mMessageHandler = new Handler();
    private Handler mConsumeHandler = new Handler();

    // Create runnable for posting back to main thread
    final Runnable mReturnMessage = new Runnable() {
        public void run() {
            mOnReceiveMessageHandler.onReceiveMessage(mLastMessage);
        }
    };

    final Runnable mConsumeRunner = new Runnable() {
        public void run() {
//            Consume();
        }
    };

    public RabbitMQConsumer(String amqpHost, String amqpExchangeName, Integer port, String username, String password, String exchangeType, String queueName, String routingKey, MainActivity activity) {
        this.amqpHost = amqpHost;
        this.port = port;
        this.username = username;
        this.password = password;
        this.amqpExchangeName = amqpExchangeName;
        this.exchangeType=exchangeType;
        this.queueName=queueName;
        this.routingKey=routingKey;

        this.activity=activity;
        executorService = Executors.newCachedThreadPool();
        disconnectHandler = new DisconnectHandler();
        blockedConnectionHandler = new BlockedConnectionHandler();
        //????可能引发问题



    }

    public void connectToRabbitMQ() throws Exception{
        try {
            mConnection = getConnection();
            mModel = createChannel(mConnection);
            mModel.basicQos(0, 10, false);
            createExchange(mModel, amqpExchangeName);
            mModel.queueDeclare(queueName, false, false, true, null);
            mModel.queueBind(queueName, amqpExchangeName, routingKey);
            // register a callback handler to receive the events that a subscriber subscribed to
            mModel.basicConsume(queueName, s_autoAck, new DefaultConsumer(mModel) {
                @Override
                public void handleDelivery(String queueName, final Envelope envelope, AMQP.BasicProperties properties, final byte[] body) throws IOException {

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                mLastMessage = body;
                                Log.v("mLastMessage ", mLastMessage.toString());
                                mMessageHandler.post(mReturnMessage);
                                try {
                                    mModel.basicAck(envelope.getDeliveryTag(), false);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception ie) {
                                ie.printStackTrace();
                            }
                        }

                    };
                    thread.start();
                }
            });
            Latte.getConfigurator().withConnectRabbit(true);
            Latte.getHandler().post(new Runnable() {
                @Override
                public void run() {
                      Resources resources=activity.getResources();;//获取本地资源
//                    RelativeLayout relativeLayout=activity.findViewById(R.id.layout_launch);
//                    relativeLayout.setBackground(resources.getDrawable(R.drawable.connect_rab));
                    AppCompatImageView imageView=activity.findViewById(R.id.tv_ring_icon);
                    imageView.setImageResource(R.drawable.ring_connect);
                    //imageView.setBackground(resources.getDrawable(R.drawable.ring_connect));

                }
            });

//        }catch (AlreadyClosedException closedException) {
//            s_logger.warn("Connection to AMQP service is lost. Subscription:" + queueName + " will be active after reconnection", closedException);
//        } catch (ConnectException connectException) {
//            s_logger.warn("Connection to AMQP service is lost. Subscription:" + queueName + " will be active after reconnection", connectException);
//        } catch (Exception e) {
//            throw new Exception("Failed to subscribe to event due to " + e.getMessage());
//        }
          }catch (Exception e)
          {
                e.printStackTrace();
//                Latte.getHandler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY),"APP未能正常连接消息队列，请检查设置后，并重新启动App!!!",Toast.LENGTH_LONG).show();
//
//
//                    }
//                });

                //?????可能引发错误
                myHandler.postDelayed(ConnectRabbitmqThread,1000);
          }
    }

    Runnable ConnectRabbitmqThread=new Runnable() {
        @Override
        public void run() {
            try {
                mConnection = getConnection();
                mModel = createChannel(mConnection);
                mModel.basicQos(0, 10, false);
                createExchange(mModel, amqpExchangeName);
                mModel.queueDeclare(queueName, false, false, true, null);
                mModel.queueBind(queueName, amqpExchangeName, routingKey);
                // register a callback handler to receive the events that a subscriber subscribed to
                mModel.basicConsume(queueName, s_autoAck, new DefaultConsumer(mModel) {
                    @Override
                    public void handleDelivery(String queueName, final Envelope envelope, AMQP.BasicProperties properties, final byte[] body) throws IOException {

                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    mLastMessage = body;
                                    Log.v("mLastMessage ", mLastMessage.toString());
                                    mMessageHandler.post(mReturnMessage);
                                    try {
                                        mModel.basicAck(envelope.getDeliveryTag(), false);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } catch (Exception ie) {
                                    ie.printStackTrace();
                                }
                            }

                        };
                        thread.start();
                    }
                });
                //可以去掉类似更新，在activity中启动了专门的线程用于更新ｒａｂｂｉｔ连接图标，可以不用单独更新连接图标
                Latte.getConfigurator().withConnectRabbit(true);
                Latte.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        AppCompatImageView imageView=activity.findViewById(R.id.tv_ring_icon);
                        imageView.setImageResource(R.drawable.ring_connect);

                    }
                });
//        }catch (AlreadyClosedException closedException) {
//            s_logger.warn("Connection to AMQP service is lost. Subscription:" + queueName + " will be active after reconnection", closedException);
//        } catch (ConnectException connectException) {
//            s_logger.warn("Connection to AMQP service is lost. Subscription:" + queueName + " will be active after reconnection", connectException);
//        } catch (Exception e) {
//            throw new Exception("Failed to subscribe to event due to " + e.getMessage());
//        }
            }catch (Exception e)
            {
                e.printStackTrace();
//                Latte.getHandler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY),"APP未能正常连接消息队列，请检查设置后，并重新启动App!!!",Toast.LENGTH_LONG).show();
//
//                    }
//                });
                myHandler.postDelayed(ConnectRabbitmqThread,1000);
            }

        }
    };




    private synchronized Connection getConnection() throws Exception {
        if (s_connection == null) {
            try {
                return createConnection();
            } catch (KeyManagementException | NoSuchAlgorithmException | IOException  | TimeoutException e) {
                s_logger.error(String.format("Failed to create a connection to AMQP server [AMQP host:%s, port:%d] due to: %s", amqpHost, port, e));
                throw e;
            }
        } else {
            return s_connection;
        }
    }
    // logic to deal with loss of connection to AMQP server
    private class DisconnectHandler implements ShutdownListener {

        @Override
        public void shutdownCompleted(ShutdownSignalException shutdownSignalException) {
            if (!shutdownSignalException.isInitiatedByApplication()) {

                abortConnection(); // disconnected to AMQP server, so abort the connection and channels
                //rabbitmqdu断开后切换到启动界面
                Latte.getConfigurator().withConnectRabbit(false);
//                activity.start(new LauncherDelegate(),1);
//                Latte.getHandler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Resources resources=activity.getResources();;//获取本地资源
//                        RelativeLayout relativeLayout=activity.findViewById(R.id.layout_launch);
//                        relativeLayout.setBackground(resources.getDrawable(R.drawable.disconnect_rab));
//
//                    }
//                });
                Latte.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        AppCompatImageView imageView=activity.findViewById(R.id.tv_ring_icon);
                        imageView.setImageResource(R.drawable.ring_disconnect);

                    }
                });
                // initiate re-connect process
                ReconnectionTask reconnect = new ReconnectionTask();
                executorService.submit(reconnect);
            }
        }
    }

    //logic to deal with blocked connection. connections are blocked for example when the rabbitmq server is out of space. https://www.rabbitmq.com/connection-blocked.html
    private class BlockedConnectionHandler implements BlockedListener {

        @Override
        public void handleBlocked(String reason) throws IOException {
//            s_logger.error("rabbitmq connection is blocked with reason: " + reason);
            closeConnection();
            Latte.getConfigurator().withConnectRabbit(false);
            try {
                throw new Exception("unblocking the parent thread as publishing to rabbitmq server is blocked with reason: " + reason);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handleUnblocked() throws IOException {
            s_logger.info("rabbitmq connection in unblocked");
        }
    }
    private synchronized void abortConnection() {
        if (s_connection == null)
            return;

        try {
            s_connection.abort();

        } catch (Exception e) {
//            s_logger.warn("Failed to abort connection due to " + e.getMessage());
        }
        s_connection = null;
    }
    private synchronized void closeConnection() {
        try {
            if (s_connection != null) {
                s_connection.close();
            }
        } catch (Exception e) {
//            s_logger.warn("Failed to close connection to AMQP server due to " + e.getMessage());
        }
        s_connection = null;
    }

    // retry logic to connect back to AMQP server after loss of connection
    private class ReconnectionTask implements Runnable {
        boolean connected = false;
        Connection connection = null;
        @Override
        public void run() {

            while (!connected) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    // ignore timer interrupts
                }

                try {
                    try {
                        connection = createConnection();
                        connected = true;
                    } catch (IOException ie) {
                        continue; // can't establish connection to AMQP server yet, so continue
                    }

                        /** create a queue with subscriber ID as queue name and bind it to the exchange
                         *  with binding key formed from event topic
                         */
                        final Channel channel = createChannel(connection);
                        channel.basicQos(0, 10, false);
                        createExchange(channel, amqpExchangeName);

                        channel.queueDeclare(queueName, false, false, true, null);
                        channel.queueBind(queueName, amqpExchangeName, routingKey);

                        // register a callback handler to receive the events that a subscriber subscribed to
                        channel.basicConsume(queueName, s_autoAck, new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(String queueName, final Envelope envelope, AMQP.BasicProperties properties, final byte[] body) throws IOException {

                                Thread thread = new Thread()
                                {
                                    @Override
                                    public void run() {
                                                try {
                                                mLastMessage = body;
                                                Log.v("mLastMessage ", mLastMessage.toString());
                                                mMessageHandler.post(mReturnMessage);
                                                try {
                                                    channel.basicAck(envelope.getDeliveryTag(), false);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            } catch (Exception ie) {
                                                ie.printStackTrace();
                                            }
                                        }

                                };
                                thread.start();



                            }
                        });
                    //如果重练成功则切换连接成功的底图, //rabbitmqdu断开后切换到启动界面
                    //                activity.start(new LauncherDelegate());和对应
                    Latte.getConfigurator().withConnectRabbit(true);
                    Latte.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
//                            Resources resources=activity.getResources();;//获取本地资源
//                            RelativeLayout relativeLayout=activity.findViewById(R.id.layout_launch);
//                            relativeLayout.setBackground(resources.getDrawable(R.drawable.connect_rab));
                            AppCompatImageView imageView=activity.findViewById(R.id.tv_ring_icon);
                            imageView.setImageResource(R.drawable.ring_connect);

                        }
                    });

                } catch (Exception e) {
//                    s_logger.warn("Failed to recreate queues and binding for the subscribers due to " + e.getMessage());
                }
            }
            return;
        }
    }
    private synchronized Connection createConnection() throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setHost(amqpHost);
        factory.setPort(port);
        //
        factory.setRequestedHeartbeat(1);

        Connection connection = factory.newConnection();
        connection.addShutdownListener(disconnectHandler);
        connection.addBlockedListener(blockedConnectionHandler);
        s_connection = connection;
        return s_connection;
    }

    private Channel createChannel(Connection connection) throws Exception {
        try {
            return connection.createChannel();
        } catch (IOException exception) {
//            s_logger.warn("Failed to create a channel due to " + exception.getMessage());
            throw exception;
        }
    }
    private void createExchange(Channel channel, String exchangeName) throws Exception {
        try {
            channel.exchangeDeclare(exchangeName, exchangeType, false,true,null);
        } catch (IOException exception) {
//            s_logger.error("Failed to create exchange" + exchangeName + " on RabbitMQ server");
            throw exception;
        }
    }
    public void Dispose()
    {
        try {
            if (mModel != null)
                mModel.close();
            if (mConnection!=null)
                mConnection.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }
}
