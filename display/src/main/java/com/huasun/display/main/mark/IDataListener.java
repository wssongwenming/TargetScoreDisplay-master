package com.huasun.display.main.mark;

import com.bcsb.rabbitmq.entity.MarkData;

/**
 * author:songwenming
 * Date:2019/10/13
 * Description:
 */
public interface IDataListener {
    MarkData[] getMarkData();
}
