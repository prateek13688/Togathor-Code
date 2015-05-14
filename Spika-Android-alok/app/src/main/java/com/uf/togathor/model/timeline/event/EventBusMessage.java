package com.uf.togathor.model.timeline.event;

import com.uf.togathor.db.couchdb.model.Group;

/**
 * Created by Alok on 4/6/2015.
 */
public class EventBusMessage {
    private EventMessage eventMessage;
    private Group eventGroup;

    public EventBusMessage(EventMessage eventMessage, Group eventGroup) {
        this.eventGroup = eventGroup;
        this.eventMessage = eventMessage;
    }

    public EventMessage getEventMessage() {
        return eventMessage;
    }

    public Group getEventGroup() {
        return eventGroup;
    }
}
