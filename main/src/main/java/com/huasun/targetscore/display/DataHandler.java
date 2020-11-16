package com.huasun.targetscore.display;

import com.huasun.core.util.SoundPoolUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * author:songwenming
 * Date:2020/11/12
 * Description:
 */
class DataHandler {

    private LinkedList<String> linkedList = new LinkedList<String>();

    public static final int BATCH_SIZE = 1;

    private static final DataHandler instance = new DataHandler();

    private DataHandler() {
        //监听线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (linkedList.size() >= BATCH_SIZE) {
//                            String[] sendData = new String[BATCH_SIZE];
                            for (int i = 0; i < BATCH_SIZE; i++) {
//                            sendData[i] = linkedList.removeFirst();
                              String ringNumberOffset=linkedList.removeFirst();
                              List<String> ringNumber_OffsetList = Arrays.asList(ringNumberOffset.split(","));
                              SoundPoolUtil.getInstance().play(ringNumber_OffsetList, 0) ;
                            }
                        }
                        Thread.sleep(50);
                    } catch (InterruptedException ignore) {}
                }
            }

        }).start();

    }

    /**
     * 单例实现
     * @return
     */
    public static DataHandler getInstance() {
        return instance;
    }
    /**
     * 添加数据
     * @param ringnumber
     */
    public void addData(String ringnumber,String offset) {
        linkedList.addLast(ringnumber+","+offset);
    }

}