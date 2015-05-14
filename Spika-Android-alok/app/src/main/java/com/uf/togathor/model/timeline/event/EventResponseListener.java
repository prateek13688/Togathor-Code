package com.uf.togathor.model.timeline.event;

/**
 * Created by Alok on 4/6/2015.
 */
public interface EventResponseListener {
    public void registerForResponse();
    public void onEvent(EventBusMessage eventResponse);
}
