package com.cysion.tdframework.proxy;

import com.cysion.tdframework.base.TdEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by cysion on 2016/6/19 0019.
 * proxy of eventbus
 * should be used with {@link TdEvent}
 */
public class MsgProxy {

    private static volatile MsgProxy instance = new MsgProxy();

    private EventBus mBus;

    private MsgProxy() {
        mBus = EventBus.getDefault();
    }

    public static synchronized MsgProxy getInstance() {
        return instance;
    }

    public void register(Object subscriber) {
        mBus.register(subscriber);
    }

    public void unRegister(Object subscriber) {
        mBus.unregister(subscriber);
    }

    public void post(TdEvent event) {
        mBus.post(event);
    }

    public void postSticky(TdEvent event) {
        mBus.postSticky(event);
    }

    public void isRegistered(Object subscriber) {
        mBus.isRegistered(subscriber);
    }

    public void cancelEventDelivery(TdEvent event) {
        mBus.cancelEventDelivery(event);
    }

    public void hasSubscriberForEvent(Class aClass) {
        mBus.hasSubscriberForEvent(aClass);
    }

    public void removeStickyEvent(Object event) {
        mBus.removeStickyEvent(event);
    }

    public void removeAllStickyEvents() {
        mBus.removeAllStickyEvents();
    }
}
