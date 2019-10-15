package com.huasun.targetscore.rabbitmq;

import android.os.Handler;
import android.util.Log;

import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

/**
*工作流程:在需要监听的地方实例化MessageConsumer，然后连接,
*/
public class MessageConsumer extends  IConnectToRabbitMQ{

  public MessageConsumer(String server, String exchange, String exchangeType,int port,String username,String password) {
      super(server, exchange, exchangeType, port,username,password);
  }
  //The Queue name for this consumer
  private String mQueue;
  private QueueingConsumer MySubscription;
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
          Consume();
      }
  };

  /**
   * Create Exchange and then start consuming. A binding needs to be added before any messages will be delivered
   */

  public boolean connectToCommandRabbitMQ()
  {
     if(super.connectToRabbitMQ())
     {

         try {
             mQueue = mModel.queueDeclare("signin-queue",true,false,false,null).getQueue();

             MySubscription = new QueueingConsumer(mModel);

             mModel.basicConsume(mQueue, false, MySubscription);

          } catch (IOException e) {
              e.printStackTrace();
              return false;
          }
           if (MyExchangeType == "topic")
                 AddBinding("signinway.*");//fanout has default binding

          Running = true;
          mConsumeHandler.post(mConsumeRunner);//在一个新的线程里开启消息阻塞获取模式
         return true;
     }
     return false;
  }

  /**
   * Add a binding between this consumers Queue and the Exchange with routingKey
   * @param routingKey the binding key eg GOOG
   */
  public void AddBinding(String routingKey)
  {
      try {
          mModel.queueBind("signin-queue", "bcsb-exchange", routingKey);
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

  private void Consume()
  {
      Thread thread = new Thread()
      {
           @Override
              public void run() {
               while(Running){
                  QueueingConsumer.Delivery delivery;
                  try {
                      delivery = MySubscription.nextDelivery();//DG当前线程被阻塞，直到有消息来到
                      mLastMessage = delivery.getBody();
                      mMessageHandler.post(mReturnMessage);//通过handler将消息处理线程抛回主线程
                      try {
                          mModel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                  } catch (InterruptedException ie) {
                      ie.printStackTrace();
                  }
               }
           }
      };
      thread.start();

  }

  public void dispose(){
      Running = false;
  }
}
