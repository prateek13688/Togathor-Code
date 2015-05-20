package com.uf.togathor.management;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.uf.togathor.HomeActivity;
import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.TogathorException;
import com.uf.togathor.db.couchdb.TogathorForbiddenException;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.model.Message;
import com.uf.togathor.model.timeline.event.EventMessage;
import com.uf.togathor.modules.chat.JoinGroupActivity;
import com.uf.togathor.utils.appservices.EventService;
import com.uf.togathor.utils.constants.Const;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by alok on 1/27/15.
 */
public class SyncModule {

    private static User fromUser;
    private static User toUser;
    private static Group toGroup;
    private static ArrayList<Message> listOfMessages;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static int currentPage = 0;

    public static ArrayList<Message> getMessagesFromServer(final Context context) {

        fromUser = UsersManagement.getLoginUser();
        sharedPreferences = context.getSharedPreferences(HomeActivity.class.getName(), Context.MODE_MULTI_PROCESS);
        editor = sharedPreferences.edit();
        toUser = UsersManagement.getToUser();
        toGroup = UsersManagement.getToGroup();

        if (toUser != null)
            currentPage = sharedPreferences.getInt("page" + toUser.getId(), 0);
        else
            currentPage = sharedPreferences.getInt("page" + toGroup.getId(), 0);

        try {
            listOfMessages = (new CouchDB.FindMessagesForUser(fromUser, toUser, toGroup, currentPage).execute()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        if (toUser != null)
            editor.putInt("page" + toUser.getId(), (currentPage + 1));
        else
            editor.putInt("page" + toGroup.getId(), (currentPage + 1));

        editor.apply();

        return listOfMessages;
    }

    public static ArrayList<Message> getAllMessagesFromServer() {

        List<Message> tempList;
        listOfMessages = new ArrayList<>();

        fromUser = UsersManagement.getLoginUser();
        toUser = UsersManagement.getToUser();
        toGroup = UsersManagement.getToGroup();
        currentPage = 0;

        try {
            while (true) {
                tempList = (new CouchDB.FindMessagesForUser(fromUser, toUser, toGroup, currentPage++)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)).get();
                if (tempList == null || tempList.size() == 0)
                    break;

                listOfMessages.addAll(tempList);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return listOfMessages;
    }

    public static List<User> getAllUserContactsFromServer() {

        List<User> tempList = new ArrayList<>();

        fromUser = UsersManagement.getLoginUser();

        try {
            tempList = new AsyncTask<Void, Void, List<User>>() {
                @Override
                protected List<User> doInBackground(Void... params) {
                    try {
                        return new CouchDB.FindUserContacts(fromUser.getId()).execute();
                    } catch (TogathorException | TogathorForbiddenException | JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    return new ArrayList<>();
                }
            }.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return tempList;
    }

    public static List<Group> getAllUserGroupsFromServer() {

        List<Group> tempList = new ArrayList<>();

        fromUser = UsersManagement.getLoginUser();

        try {
            tempList = new AsyncTask<Void, Void, List<Group>>() {
                @Override
                protected List<Group> doInBackground(Void... params) {
                    try {
                        return new CouchDB.FindUserGroups(fromUser.getId()).execute();
                    } catch (TogathorException | TogathorForbiddenException | JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    return new ArrayList<>();
                }
            }.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return tempList;
    }

    public static List<User> getAllUserContacts() {

        List<User> tempList = new ArrayList<>();

        fromUser = UsersManagement.getLoginUser();

        for (Map.Entry<String, User> entry :
                Togathor.getContactsDataSource().getAllUserContacts().entrySet()) {
            tempList.add(entry.getValue());
        }

        return tempList;
    }

    public static List<Group> getAllUserGroups() {

        List<Group> tempList = new ArrayList<>();

        fromUser = UsersManagement.getLoginUser();

        for (Map.Entry<String, Group> entry :
                Togathor.getContactsDataSource().getAllUserGroups().entrySet()) {
            tempList.add(entry.getValue());
        }

        return tempList;
    }

    public static ArrayList<Message> getAllEventsFromServer() {

        List<Message> tempList;

        fromUser = UsersManagement.getLoginUser();
        currentPage = 0;

        if (Togathor.getEventService() == null) {
            Log.d("SyncModule", "The Event Service is not running!");
            Log.d("SyncModule", "Starting the event service");
            Togathor.getInstance().startEventService();
        }

        listOfMessages = new ArrayList<>();
        for (Group timelineGroup : Togathor.getEventService().timelineGroups) {
            try {
                while (true) {
                    tempList = (new CouchDB.FindMessagesForUser(fromUser, null, timelineGroup, currentPage++)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)).get();
                    if (tempList == null || tempList.size() == 0)
                        break;

                    listOfMessages.addAll(tempList);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
        return listOfMessages;
    }

    public static ArrayList<Message> getEventsFromServerFor(Group group) {

        List<Message> tempList;

        fromUser = UsersManagement.getLoginUser();
        currentPage = 0;

        if (Togathor.getEventService() == null) {
            Log.d("SyncModule", "The Event Service is not running!");
            Log.d("SyncModule", "Starting the event service");
            Togathor.getInstance().startEventService();
        }

        listOfMessages = new ArrayList<>();
        try {
            while (true) {
                tempList = (new CouchDB.FindMessagesForUser(fromUser, null, group, currentPage++)).execute().get();
                if (tempList == null || tempList.size() == 0)
                    break;

                listOfMessages.addAll(tempList);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return listOfMessages;
    }

    public static boolean storeInLocalDB(Message message, Context context, User toUser, Group toGroup) {
        EventMessage eventMessage;
        if (toGroup != null && toGroup.getCategoryId().equals(Const.TIMELINE_GROUP_ID)) {
            eventMessage = EventService.parseMessage(message);
            Togathor.getEventService().processEvent(eventMessage, toGroup);
            return true;
        }

        try {
            if (toUser != null)
                Togathor.getMessagesDataSource().insertMessage(message, toUser);
            else if (toGroup != null)
                Togathor.getMessagesDataSource().insertMessage(message, toGroup);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void addUserContact(final String userID, final Context context) {
        if (Togathor.getContactsDataSource().getAllUserContacts().containsKey(userID))
            return;

        CouchDB.addUserContactAsync(userID, new ResultListener<Boolean>() {
            @Override
            public void onResultsSucceeded(Boolean result) {
                CouchDB.findUserByIdAsync(userID, new ResultListener<User>() {
                    @Override
                    public void onResultsSucceeded(User result) {
                        Togathor.getContactsDataSource().insertUser(result);
                    }

                    @Override
                    public void onResultsFail() {

                    }
                }, context, true);
            }

            @Override
            public void onResultsFail() {

            }
        }, context, true);
    }

    public static void addGroupContact(final String groupID, final String userID, final Context context) {
        Boolean result = Togathor.getContactsDataSource().getAllUserGroups().containsKey(groupID);
        if (result)
            Toast.makeText(context, context.getString(R.string.ALREADY_ADDED_TO_GROUP), Toast.LENGTH_SHORT).show();
        else
        {
        CouchDB.addFavoriteGroupAsync(groupID, userID,
                new ResultListener<Boolean>() {
                    @Override
                    public void onResultsSucceeded(Boolean result) {
                        CouchDB.findGroupByIdAsync(groupID, new ResultListener<Group>() {
                            @Override
                            public void onResultsSucceeded(Group result) {
                                Togathor.getContactsDataSource().insertGroup(result);
                                    Toast.makeText(context, context.getString(R.string.ADDED_TO_GROUP), Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onResultsFail() {

                            }
                        }, context, true);
                    }

                    @Override
                    public void onResultsFail() {

                    }
                }, context, true);
    }

    }

    public static boolean sendMessage(Message message, final Context context, User toUser, Group toGroup) {
        boolean success;

        try {
            success = (new SendMessageAsync(message, context, toUser, toGroup)).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }

        return success;
    }

    private static class SendMessageAsync extends AsyncTask<Void, Void, Boolean> {

        private Message message;
        private Context context;
        private User toUser;
        private Group toGroup;

        public SendMessageAsync(Message message, Context context, User toUser, Group toGroup) {
            this.message = message;
            this.context = context;
            this.toUser = toUser;
            this.toGroup = toGroup;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            try {
                if (toUser != null) {
                    success = CouchDB.sendMessageToUser(message);
                    if (success)
                        success = SyncModule.storeInLocalDB(message, context, toUser, null);
                }
                if (toGroup != null) {
                    success = CouchDB.sendMessageToGroup(message);
                    if (success)
                        success = SyncModule.storeInLocalDB(message, context, null, toGroup);
                }
            } catch (IOException | JSONException | TogathorException | TogathorForbiddenException e) {
                e.printStackTrace();
            }

            return success;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    public static User getUserContact(String id) {
        HashMap<String, User> contacts = Togathor.getContactsDataSource().getAllUserContacts();
        return contacts.get(id);
    }

    public static Group getGroupContact(String id) {
        HashMap<String, Group> contacts = Togathor.getContactsDataSource().getAllUserGroups();
        return contacts.get(id);
    }
}
