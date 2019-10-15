package com.huasun.display.recycler;

import java.util.ArrayList;

/**
 * author:songwenming
 * Date:2019/10/14
 * Description:
 */
public abstract class DataConverter {
    protected final ArrayList<MultipleItemEntity> ENTITIES=new ArrayList<>();
    private String mJsonData=null;
    public abstract ArrayList<MultipleItemEntity>convert();
    public DataConverter setJsonData(String json){
        this.mJsonData=json;
        return this;
    }
    protected String getJsonData(){
        if(mJsonData==null||mJsonData.isEmpty()){
            throw new NullPointerException("DATA IS NULL");
        }
        return mJsonData;
    }
}
