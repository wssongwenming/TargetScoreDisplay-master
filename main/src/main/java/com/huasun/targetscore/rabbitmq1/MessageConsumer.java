//package com.huasun.targetscore.rabbitmq1;
//
//import android.os.Handler;
//
//import com.rabbitmq.client.QueueingConsumer;
//
//import java.io.IOException;
//
///**
//*工作流程:在需要监听的地方实例化MessageConsumer，然后连接,
//*/
//public class MessageConsumer extends  IConnectToRabbitMQ{
//
//  public MessageConsumer(String server, String exchange, String exchangeType,int port,String username,String password) {
//      super(server, exchange, exchangeType, port,username,password);
//  }
//  //The Queue name for command consumer
//  private String mCommandQueue;
//  private QueueingConsumer MyCommandSubscription;//命令消费者
//  //last message to post back
//  private byte[] mLastCommandMessage;
//  //射击成绩数据
//  private String mMarkDataQueue;
//  private QueueingConsumer MyMarkDataSubscription;//靶环数据消费者
//  private byte[]mLastMarkDataMessage;
//
//
//  // An interface to be implemented by an object that is interested in messages(listener)
//  public interface OnReceiveCommandMessageHandler{
//      public void onReceiveMessage(byte[] message);
//  };
//  public interface OnReceiveMarkDataHandler{
//      public void onReceiveMessage(byte[]message);
//  }
//  //A reference to the listener, 监听指令
//  private OnReceiveCommandMessageHandler mOnReceiveCommandMessageHandler;
//  //监听射击成绩
//  private OnReceiveMarkDataHandler mOnReceiveMarkDataHandler;
//  /**
//   *
//   * Set the callback for received messages
//   * @param handler The callback
//   */
//  public void setOnReceiveCommandMessageHandler(OnReceiveCommandMessageHandler handler){
//      mOnReceiveCommandMessageHandler = handler;
//  };
//  public void setOnReceiveMarkDataHandler(OnReceiveMarkDataHandler handler){
//      mOnReceiveMarkDataHandler=handler;
//  }
//
//
//  private Handler mMessageHandler = new Handler();
//  private Handler mConsumeHandler = new Handler();
//
//  // Create runnable for posting back to main thread
//  final Runnable mReturnCommandMessage = new Runnable() {
//      public void run() {
//          mOnReceiveCommandMessageHandler.onReceiveMessage(mLastCommandMessage);
//      }
//  };
//
//    final Runnable mReturnMarkDataMessage = new Runnable() {
//        public void run() {
//            mOnReceiveMarkDataHandler.onReceiveMessage(mLastMarkDataMessage);
//        }
//    };
//
//    final Runnable mConsumeCommandRunner = new Runnable() {
//      public void run() {
//          ConsumeCommandMessage();
//      }
//  };
//
//  final Runnable mConsumeMarkDataRunner=new Runnable() {
//      @Override
//      public void run() {
//          ConsumeMarkDataMessage();
//      }
//  };
//
//   /**
//   * Create Exchange and then start consuming. A binding needs to be added before any messages will be delivered
//   */
//
//  public  boolean connectToCommandRabbitMQ(String queueName,String exchangeName,String routingKey)
//  {
//     if(super.connectToRabbitMQ())
//     {
//         try {
//
//             //queueDeclare (String queue , boolean durable , boolean exclusive , boolean autoDelete , Map arguments)
//             mCommandQueue = mModel.queueDeclare(queueName,false,false,false,null).getQueue();
//
//             MyCommandSubscription = new QueueingConsumer(mModel);
//             // 将消费者绑定到队列，并设置手动确认消息（即无需显示确认，如何设置请慎重考虑）
//             mModel.basicConsume(mCommandQueue, false, MyCommandSubscription);
//
//          } catch (IOException e) {
//              e.printStackTrace();
//              return false;
//          }
//           if (MyExchangeType == "topic"||MyExchangeType == "direct")
//                 AddBindingQueue(queueName,exchangeName,routingKey);
//
//         commandConsumerRunning = true;
//         mConsumeHandler.post(mConsumeCommandRunner);//一连接然后在一个新的线程里开启消息阻塞获取模式
//         return true;
//     }
//     return false;
//  }
//
//  public boolean connectToMarkDataRabbitMQ(String queueName,String exchangeName,String routingKey)
//    {
//        if(super.connectToRabbitMQ())
//        {
//            try {
//                mMarkDataQueue =mModel.queueDeclare(queueName,false,false,false,null).getQueue();
//
//                MyMarkDataSubscription = new QueueingConsumer(mModel);
//
//                mModel.basicConsume(mMarkDataQueue, false, MyMarkDataSubscription);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            }
//            if (MyExchangeType == "topic"||MyExchangeType == "direct")
//                AddBindingQueue(queueName,exchangeName,routingKey);//fanout has default binding
//
//            markDataConsumerRunning = true;
//            mConsumeHandler.post(mConsumeMarkDataRunner);//在一个新的线程里开启消息阻塞获取模式
//            return true;
//        }
//        return false;
//    }
//
//   /**
//   * Add a binding between this consumers Queue and the Exchange with routingKey
//   * @param routingKey the binding key eg GOOG
//   */
//  public void AddBindingQueue(String queueName,String exchangName,String routingKey)
//  {
//      try {
//          mModel.queueBind(queueName, exchangName, routingKey);
//      } catch (IOException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//      }
//  }
//  /**
//   * Remove binding between this consumers Queue and the Exchange with routingKey
//   * @param routingKey the binding key eg GOOG
//   */
//  public void RemoveBinding(String routingKey)
//  {
//      try {
//          mModel.queueUnbind(mCommandQueue, mExchange, routingKey);
//      } catch (IOException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//      }
//  }
//
//  private void ConsumeCommandMessage()
//  {
//      Thread thread = new Thread()
//      {
//           @Override
//              public void run() {
//               while(commandConsumerRunning){
//                  QueueingConsumer.Delivery delivery;
//                  try {
//                      delivery = MyCommandSubscription.nextDelivery();//DG当前线程被阻塞，直到有消息来到
//                      mLastCommandMessage = delivery.getBody();
//                      mMessageHandler.post(mReturnCommandMessage);//通过handler将消息处理线程抛回主线程
//                      try {
//                          mModel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                      } catch (IOException e) {
//                          e.printStackTrace();
//                      }
//                  } catch (InterruptedException ie) {
//                      ie.printStackTrace();
//                  }
//               }
//           }
//      };
//      thread.start();
//  }
//
//    private void ConsumeMarkDataMessage()
//    {
//        Thread thread = new Thread()
//        {
//            @Override
//            public void run() {
//                while(markDataConsumerRunning){
//                    QueueingConsumer.Delivery delivery;
//                    try {
//                        delivery = MyMarkDataSubscription.nextDelivery();//DG当前线程被阻塞，直到有消息来到
//                        mLastMarkDataMessage = delivery.getBody();
//                        mMessageHandler.post(mReturnMarkDataMessage);//通过handler将消息处理线程抛回主线程
//                        try {
//                            mModel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    } catch (InterruptedException ie) {
//                        ie.printStackTrace();
//                    }
//                }
//            }
//        };
//        thread.start();
//    }
//    enum Action {
//        ACCEPT,  // 处理成功
//        RETRY,   // 可以重试的错误
//        REJECT,  // 无需重试的错误
//    }
//
///*  public void dispose(){
//      Running = false;
//  }*/
//}
