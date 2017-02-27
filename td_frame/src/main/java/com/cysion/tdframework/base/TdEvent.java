package com.cysion.tdframework.base;

/**
 * Created by cysion on 2016/6/19 0019.
 * 消息对象的基类，主要是让消息对象具有tag属性
 */
public class TdEvent {

    protected String tag = "toAdd";

    public String getTag() {
        return tag;
    }

    public void setTag(String aTag) {
        tag = aTag;
    }
}
