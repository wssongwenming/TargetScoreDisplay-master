package com.huasun.display.recycler;

import java.util.LinkedHashMap;
import java.util.WeakHashMap;

/**
 * author:songwenming
 * Date:2019/10/14
 * Description:
 */
public class MultipleEntityBuilder {
    private static final LinkedHashMap<Object,Object> FIELDS=new LinkedHashMap<>();

    public MultipleEntityBuilder() {
        FIELDS.clear();
    }
    public final MultipleEntityBuilder setItemType(int itemType){
        FIELDS.put(MultipleFields.ITEM_TYPE,itemType);
        return this;

    }
    public final MultipleEntityBuilder setFiled(Object key,Object value){
        FIELDS.put(key,value);
        return this;

    }
    public final MultipleEntityBuilder setFileds(WeakHashMap<?,?> map){
        FIELDS.putAll(map);
        return this;
    }
    public final MultipleItemEntity build(){
        return new MultipleItemEntity(FIELDS);
    }
}
