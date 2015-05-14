/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.uf.togathor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.dexafree.materialList.model.Card;
import com.google.android.gcm.GCMBaseIntentService;
import com.uf.togathor.modules.chat.ChatGroupActivity;
import com.uf.togathor.modules.chat.ChatUserActivity;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.TogathorException;
import com.uf.togathor.db.couchdb.TogathorForbiddenException;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.model.Message;
import com.uf.togathor.uitems.cards.CustomImageCard;
import com.uf.togathor.uitems.cards.CustomTextCard;
import com.uf.togathor.utils.appservices.Logger;
import com.uf.togathor.utils.constants.Const;

import org.json.JSONException;

import java.io.IOException;

/**
 * GCMIntentService
 * <p/>
 * Handles push broadcast and generates HookUp notification if application is in
 * foreground or Android notification if application is in background.
 */
public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService() {
        super(Const.PUSH_SENDER_ID);
    }

    private final String TAG = "=== GCMIntentService ===";

    enum UserSessionType {
        CURRENT_DESTINATION,
        DESTINATION_IN_BACKGROUND,
        APP_IN_BACKGROUND,
        TIMELINE
    }

    /**
     * Method called on device registered
     */
    @Override
    protected void onRegistered(Context context, String registrationId) {
        if (registrationId != null) {
            Togathor.getGoogleAPIService().savePushTokenAsync(registrationId, Const.ONLINE, context);
        }
    }

    /**
     * Method called on device unregistered
     */
    @Override
    protected void onUnregistered(Context context, String registrationId) {

        if (registrationId != null) {
            removePushTokenAsync(context);
        }
    }

    /**
     * Method called on Receiving a new message
     */
    @Override
    protected void onMessage(Context context, Intent intent) {

        final Card card;
        Message message = null;
        User fromUser;
        Group fromGroup;
        UserSessionType isCurrentDestination;

        String pushMessage = intent.getStringExtra(Const.PUSH_MESSAGE);
        String pushFromId = intent.getStringExtra(Const.PUSH_FROM_USER_ID);
        String messageId = intent.getStringExtra(Const.MESSAGE_ID);
        String groupId = intent.getStringExtra(Const.PUSH_FROM_GROUP_ID);

        if (Togathor.getPreferences().getUserId().equals(pushFromId))
            return;

        try {
            fromUser = CouchDB.findUserById(pushFromId);
            if (fromUser != null) {
                if (messageId != null)
                    message = CouchDB.findMessageById(messageId);
            }

            if (message != null) {
                if (groupId.equals("")) {
                    SyncModule.storeInLocalDB(message, context, fromUser, null);
                    isCurrentDestination = checkCurrentDestination(fromUser);
                    if (isCurrentDestination == UserSessionType.CURRENT_DESTINATION) {
                        card = buildCardFrom(message);
                        if (card != null) {
                            ChatUserActivity.gCurrentMessages.add(message);
                            ChatUserActivity.sInstance.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ChatUserActivity.chatListView.add(card);
                                    ChatUserActivity.chatListView.setSelection(ChatUserActivity.chatListView.getCount() - 1);
                                }
                            });
                        }
                    } else if (isCurrentDestination == UserSessionType.DESTINATION_IN_BACKGROUND) {
                        buildNotification(context, pushMessage, fromUser, null);
                    } else if (isCurrentDestination == UserSessionType.APP_IN_BACKGROUND) {
                        buildNotification(context, pushMessage, fromUser, null);
                    }
                } else {
                    fromGroup = CouchDB.findGroupById(groupId);
                    SyncModule.storeInLocalDB(message, context, null, fromGroup);
                    isCurrentDestination = checkCurrentDestination(fromGroup);
                    if (isCurrentDestination == UserSessionType.CURRENT_DESTINATION) {
                        card = buildCardFrom(message);
                        if (card != null) {
                            ChatGroupActivity.gCurrentMessages.add(message);
                            ChatGroupActivity.sInstance.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ChatGroupActivity.chatListView.add(card);
                                    ChatGroupActivity.chatListView.setSelection(ChatGroupActivity.chatListView.getCount() - 1);
                                }
                            });
                        }
                    } else if (isCurrentDestination == UserSessionType.DESTINATION_IN_BACKGROUND) {
                        buildNotification(context, pushMessage, null, fromGroup);
                    } else if (isCurrentDestination == UserSessionType.APP_IN_BACKGROUND) {
                        buildNotification(context, pushMessage, null, fromGroup);
                    } else if (isCurrentDestination == UserSessionType.TIMELINE)    {
                        //TODO
                    }
                }
            }
        } catch (JSONException | IOException | TogathorException | TogathorForbiddenException e) {
            e.printStackTrace();
        }
    }

    private Card buildCardFrom(Message message) {
        Card card;

        if (message.getMessageType().equals(Const.TEXT)) {
            card = new CustomTextCard(R.layout.card_text_message, message);
        } else if (message.getMessageType().equals(Const.IMAGE)) {
            card = new CustomImageCard(R.layout.card_image_message, message);
        } else if (message.getMessageType().equals(Const.LOCATION)) {
            card = null;
        } else {
            card = null;
        }

        return card;
    }

    private UserSessionType checkCurrentDestination(User fromUser) {
        User currentToUser = UsersManagement.getToUser();

        if (currentToUser != null && fromUser.getId().equals(currentToUser.getId()))
            return UserSessionType.CURRENT_DESTINATION;
        else if (currentToUser != null && !fromUser.getId().equals(currentToUser.getId()))
            return UserSessionType.DESTINATION_IN_BACKGROUND;
        else
            return UserSessionType.APP_IN_BACKGROUND;
    }

    private UserSessionType checkCurrentDestination(Group fromGroup) {
        Group currentToGroup = UsersManagement.getToGroup();

        if (currentToGroup != null && fromGroup.getId().equals(currentToGroup.getId()))
            return UserSessionType.CURRENT_DESTINATION;
        else if (currentToGroup != null && !fromGroup.getId().equals(currentToGroup.getId()))
            return UserSessionType.DESTINATION_IN_BACKGROUND;
        else if (fromGroup.getCategoryId().equals(Const.TIMELINE_GROUP_ID))
            return UserSessionType.TIMELINE;
        else
            return UserSessionType.APP_IN_BACKGROUND;
    }

    /**
     * Method called on Error
     */
    @Override
    protected void onError(Context arg0, String errorId) {
        Logger.error(TAG, "Received error: " + errorId);
    }

    private void buildNotification(Context context, String pushMessage, User fromUser, Group fromGroup) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("New Message - Togathor")
                        .setContentText(pushMessage)
                        .setAutoCancel(true);
        Intent resultIntent;
        int notificationID = 1;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (fromGroup != null) {
            UsersManagement.setToGroup(fromGroup);
            resultIntent = new Intent(context, ChatGroupActivity.class);
        } else {
            UsersManagement.setToUser(fromUser);
            resultIntent = new Intent(context, ChatUserActivity.class);
        }

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationID, builder.build());
    }

    private void removePushTokenAsync(Context context) {
        Togathor.getPreferences().setUserPushToken("");
        if (UsersManagement.getLoginUser() != null) {
            CouchDB.unregisterPushTokenAsync(UsersManagement.getLoginUser().getId(), new RemovePushTokenListener(), context, false);
        }
    }

    private class RemovePushTokenListener implements ResultListener<String> {
        @Override
        public void onResultsSucceeded(String result) {
            if (result != null && result.contains("OK")) {
                Togathor.getPreferences().setUserEmail("");
                Togathor.getPreferences().setUserPassword("");
            }
        }

        @Override
        public void onResultsFail() {
        }
    }
}