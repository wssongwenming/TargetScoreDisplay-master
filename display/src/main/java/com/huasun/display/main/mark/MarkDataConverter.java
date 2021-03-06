package com.huasun.display.main.mark;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huasun.display.recycler.DataConverter;
import com.huasun.display.recycler.ItemType;
import com.huasun.display.recycler.MultipleFields;
import com.huasun.display.recycler.MultipleItemEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * author:songwenming
 * Date:2019/10/14
 * Description:
 */
public class MarkDataConverter extends DataConverter {

    @Override
    public ArrayList<MultipleItemEntity> convert() {
        ENTITIES.clear();
        final MultipleItemEntity title=MultipleItemEntity.builder()
                .setFiled(MultipleFields.ID,"序号")
                .setFiled(MultipleFields.ITEM_TYPE, ItemType.TEXT_TEXT)
                .setFiled(MultipleFields.RINGNUMBER,"环数")
                .setFiled(MultipleFields.OFFSETDIRECTION,"偏移")
                .setFiled(MultipleFields.SHOOTINGTIE,"时间")
                .build();
//        ENTITIES.add(title);
        String json=getJsonData();
        if(json!=null&&!json.isEmpty()) {
            final JSONArray holeArray = JSON.parseObject(json).getJSONArray("holes");
//          2020.10.30添加可能引发错误
            final JSONArray increasedRingNumberArray=JSON.parseObject(json).getJSONArray("increasedRingNumbers");
            System.out.print("increadringnumber="+increasedRingNumberArray);
            final int size = holeArray.size();
            for (int i = 0; i < size; i++) {
                final JSONObject hole = holeArray.getJSONObject(i);
                final int id = hole.getInteger("id");
                final String ringNumber = hole.getString("score");
                final String offset = hole.getString("offset");
                final String offsetDirection=offset.equalsIgnoreCase("u")? "上":(offset.equalsIgnoreCase("d")?"下":(offset.equalsIgnoreCase("l")?"左":(offset.equalsIgnoreCase("r")?"右":(offset.equalsIgnoreCase("lu")?"左上":(offset.equalsIgnoreCase("ru")?"右上":(offset.equalsIgnoreCase("ld")?"左下":(offset.equalsIgnoreCase("rd")?"右下":"未知")))))));
                final String shootingTime = hole.getString("shootingTime");
                int type = ItemType.TEXT_TEXT;
                final MultipleItemEntity entity = MultipleItemEntity.builder()
                        .setFiled(MultipleFields.ID, id)
                        .setFiled(MultipleFields.ITEM_TYPE, type)
                        .setFiled(MultipleFields.RINGNUMBER, ringNumber)
                        .setFiled(MultipleFields.OFFSETDIRECTION, offsetDirection)
                        .setFiled(MultipleFields.SHOOTINGTIE, shootingTime)
                        .build();
                ENTITIES.add(entity);
            }
        }
        return ENTITIES;
    }
}
