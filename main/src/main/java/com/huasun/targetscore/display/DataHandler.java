package com.huasun.targetscore.display;

import android.content.res.Resources;
import android.support.v7.widget.AppCompatTextView;
import android.widget.RelativeLayout;

import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.huasun.core.util.SoundPoolUtil;
import com.huasun.display.main.mark.MarkDelegate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * author:songwenming
 * Date:2020/11/12
 * Description:
 */
class DataHandler {

    private MainActivity mainActivity;
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
                            for (int i = 0; i < BATCH_SIZE; i++) {
//                            sendData[i] = linkedList.removeFirst();
                                String ringNumberOffset=linkedList.removeFirst();
                                final List<String> ringNumber_OffsetList = Arrays.asList(ringNumberOffset.split(","));
                                Latte.getHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        AppCompatTextView current_ringnumber_tv=mainActivity.findViewById(R.id.tv_current_ring_number);
                                        current_ringnumber_tv.setText(ringNumber_OffsetList.get(0));

                                    }
                                });
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

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public void clearData(){
        linkedList.clear();
    }
}