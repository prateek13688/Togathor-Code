package com.uf.togathor.uitems.cards;

import com.dexafree.materialList.model.Card;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.model.Message;

/**
 * Created by Alok on 3/10/2015
 */
public class CustomUserCard extends Card {
    private int resource;
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CustomUserCard(int resource, User user) {
        this.resource = resource;
        this.user = user;
    }

    @Override
    public int getLayout() {
        return resource;
    }
}