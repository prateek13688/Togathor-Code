package com.uf.togathor.modules.timeline.meetup;

import com.dexafree.materialList.model.Card;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.model.timeline.event.EventMessage;

/**
 * Created by Alok on 3/10/2015
 */
public class MeetupTimelineCard extends Card {
    private int resource;
    private EventMessage message;
    private Group eventGroup;

    public MeetupTimelineCard(int resource, EventMessage message, Group eventGroup) {
        this.resource = resource;
        this.message = message;
        this.eventGroup = eventGroup;
    }

    public Group getEventGroup() {
        return eventGroup;
    }

    public EventMessage getEventMessage() {
        return message;
    }

    public void setEventMessage(EventMessage message) {
        this.message = message;
    }

    @Override
    public int getLayout() {
        return resource;
    }
}