package com.huasun.targetscore.rabbitmq;

import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

/**
 * author:songwenming
 * Date:2020/1/12
 * Description:
 */
public class MessageProducer extends  IConnectToRabbitMQ{
    /**
     * @param server       The server address
     * @param exchange     The named exchange
     * @param exchangeType The exchange type name
     * @param port
     * @param username
     * @param password
     */
    public MessageProducer(String server, String exchange, String exchangeType, int port, String username, String password) {
        super(server, exchange, exchangeType, port, username, password);
    }

    public boolean connectToSituationRabbitMQ()
    {
        if(super.connectToRabbitMQ())
        {
            return true;
        }
        return false;
    }
}
