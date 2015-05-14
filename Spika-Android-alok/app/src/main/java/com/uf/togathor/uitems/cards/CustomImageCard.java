package com.uf.togathor.uitems.cards;

import com.dexafree.materialList.model.Card;
import com.uf.togathor.model.Message;

/**
 * Created by Alok on 3/10/2015
 */
public class CustomImageCard extends Card {
    private int resource;
    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public CustomImageCard(int resource, Message message) {
        this.resource = resource;
        this.message = message;
    }

    @Override
    public int getLayout() {
        return resource;
    }
}