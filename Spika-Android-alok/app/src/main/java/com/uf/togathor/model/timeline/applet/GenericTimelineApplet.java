package com.uf.togathor.model.timeline.applet;

import android.app.Activity;

import com.dexafree.materialList.model.Card;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.model.timeline.event.EventBusMessage;
import com.uf.togathor.model.timeline.event.EventMessage;

/**
 * Created by Alok on 4/4/2015.
 */
public interface GenericTimelineApplet {
    public void createAppletInstance();
    public void processListener(EventMessage eventMessage, Group eventGroup);
    public void processCallback(EventMessage eventMessage, Group eventGroup);
    public void stopApplet();
}
