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

package com.uf.togathor.db.couchdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uf.togathor.db.couchdb.model.Attachment;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.db.couchdb.model.GroupCategory;
import com.uf.togathor.db.couchdb.model.Member;
import com.uf.togathor.db.couchdb.model.Server;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.db.couchdb.model.UserGroup;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.model.Message;
import com.uf.togathor.utils.appservices.Logger;
import com.uf.togathor.utils.constants.Const;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * CouchDBHelper
 * 
 * Used for parsing JSON response from server.
 */
public class CouchDBHelper {

	private static String TAG = "CouchDbHelper: ";

	private static final Gson sGsonExpose = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation().create();

	/**
	 * Parse a single user JSON object
	 * 
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	public static User parseSingleUserObject(JSONObject json)
			throws JSONException {
		User user = null;
		ArrayList<String> contactsIds = new ArrayList<String>();

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				return null;
			}

			JSONArray rows = json.getJSONArray(Const.ROWS);
			JSONObject row = rows.getJSONObject(0);
			JSONObject userJson = row.getJSONObject(Const.VALUE);

			user = sGsonExpose.fromJson(userJson.toString(), User.class);
			
			if (userJson.has(Const.FAVORITE_GROUPS)) {
				JSONArray favorite_groups = userJson
						.getJSONArray(Const.FAVORITE_GROUPS);

				List<String> groups = new ArrayList<String>();

				for (int i = 0; i < favorite_groups.length(); i++) {
					groups.add(favorite_groups.getString(i));
				}

				user.setGroupIds(groups);
			}

			if (userJson.has(Const.CONTACTS)) {
				JSONArray contacts = userJson.getJSONArray(Const.CONTACTS);

				for (int i = 0; i < contacts.length(); i++) {
					contactsIds.add(contacts.getString(i));
				}

				user.setContactIds(contactsIds);
			}
		}

		return user;
	}

	   /**
     * Parse a single user JSON object
     * 
     * @param json
     * @return
     * @throws JSONException
	 * @throws TogathorException
     */
    public static User parseSingleUserObjectWithoutRowParam(JSONObject userJson)
            throws JSONException, TogathorException {
        User user = null;
        ArrayList<String> contactsIds = new ArrayList<String>();

        if (userJson != null) {
        	
        	if (userJson.length() == 0) {
        		return null;
        	}
        	
        	if (userJson.has(Const.ERROR)) {
				throw new TogathorException(ConnectionHandler.getError(userJson));
			}

            user = sGsonExpose.fromJson(userJson.toString(), User.class);
            
            if (userJson.has(Const.FAVORITE_GROUPS)) {
                JSONArray favorite_groups = userJson
                        .getJSONArray(Const.FAVORITE_GROUPS);

                List<String> groups = new ArrayList<String>();

                for (int i = 0; i < favorite_groups.length(); i++) {
                    groups.add(favorite_groups.getString(i));
                }

                user.setGroupIds(groups);
            }

            if (userJson.has(Const.CONTACTS)) {
                JSONArray contacts = userJson.getJSONArray(Const.CONTACTS);

                for (int i = 0; i < contacts.length(); i++) {
                    contactsIds.add(contacts.getString(i));
                }

                user.setContactIds(contactsIds);
            }
        }

        return user;
    }
    
	/**
	 * Parse multi JSON objects of type user
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<User> parseMultiUserObjects(JSONObject json) throws JSONException {

		List<User> users = null;
		ArrayList<String> contactsIds = new ArrayList<String>();

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				return null;
			}

			users = new ArrayList<User>();

			// Get the element that holds the users ( JSONArray )
			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				JSONObject userJson = row.getJSONObject(Const.VALUE);

				User user = new User();

				user = sGsonExpose
						.fromJson(userJson.toString(), User.class);

				if (userJson.has(Const.CONTACTS)) {

					JSONArray contacts = userJson
							.getJSONArray(Const.CONTACTS);

					for (int j = 0; j < contacts.length(); j++) {
						contactsIds.add(contacts.getString(j));
					}

					user.setContactIds(contactsIds);
				}

				if (userJson.has(Const.FAVORITE_GROUPS)) {
					JSONArray favorite_groups = userJson
							.getJSONArray(Const.FAVORITE_GROUPS);

					List<String> groups = new ArrayList<String>();

					for (int k = 0; k < favorite_groups.length(); k++) {
						groups.add(favorite_groups.getString(k));
					}

					user.setGroupIds(groups);
				}

				users.add(user);
			}
		}

		return users;
	}

	/**
	 * Parse multi JSON objects of type user for search users
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<User> parseSearchUsersResult(JSONArray jsonArray) throws JSONException {

		List<User> users = null;
		ArrayList<String> contactsIds = new ArrayList<String>();

		if (jsonArray != null) {

			users = new ArrayList<User>();

			// Get the element that holds the users ( JSONArray )

			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject userJson = jsonArray.getJSONObject(i);

				User user = new User();

				user = sGsonExpose
						.fromJson(userJson.toString(), User.class);


				if (userJson.has(Const.CONTACTS)) {

					JSONArray contacts = userJson
							.getJSONArray(Const.CONTACTS);

					for (int j = 0; j < contacts.length(); j++) {
						contactsIds.add(contacts.getString(j));
					}

					user.setContactIds(contactsIds);
				}

				if (userJson.has(Const.FAVORITE_GROUPS)) {
					JSONArray favorite_groups = userJson
							.getJSONArray(Const.FAVORITE_GROUPS);

					List<String> groups = new ArrayList<String>();

					for (int k = 0; k < favorite_groups.length(); k++) {
						groups.add(favorite_groups.getString(k));
					}

					user.setGroupIds(groups);
				}

				users.add(user);
			}
		}

		return users;
	}

	/**
	 * Parse multi JSON objects of type group for search groups
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Group> parseSearchGroupsResult(JSONArray jsonArray) throws JSONException {

		List<Group> groups = null;

		if (jsonArray != null) {

			groups = new ArrayList<Group>();

			// Get the element that holds the groups ( JSONArray )

			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject groupJson = jsonArray.getJSONObject(i);

				Group group = new Group();

				group = sGsonExpose.fromJson(groupJson.toString(),
						Group.class);

				groups.add(group);
			}
			
		}

		return groups;
	}

	/**
	 * Parse user JSON objects from get user contacts call
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<User> parseUserContacts(JSONObject json) throws JSONException {

		List<User> users = null;

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				return null;
			}

			users = new ArrayList<User>();

			// Get the element that holds the users ( JSONArray )
			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				if (!row.isNull(Const.DOC)) {
					JSONObject userJson = row.getJSONObject(Const.DOC);

					User user = new User();

					user = sGsonExpose.fromJson(userJson.toString(),
							User.class);

					if (userJson.has(Const.FAVORITE_GROUPS)) {
						JSONArray favorite_groups = userJson
								.getJSONArray(Const.FAVORITE_GROUPS);

						List<String> groups = new ArrayList<String>();

						for (int z = 0; z < favorite_groups.length(); z++) {
							groups.add(favorite_groups.getString(z));
						}

						user.setGroupIds(groups);
					}

					if (userJson.has(Const.CONTACTS)) {
						JSONArray contacts = userJson
								.getJSONArray(Const.CONTACTS);

						List<String> contactsIds = new ArrayList<String>();

						for (int j = 0; j < contacts.length(); j++) {
							contactsIds.add(contacts.getString(j));
						}
						user.setContactIds(contactsIds);
					}

					users.add(user);
				}
			}
		}

		return users;
	}

	/**
	 * Create user response object
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static String createUser(JSONObject json) throws JSONException {

		boolean ok = false;
		String id = null;

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				return null;
			}

			ok = json.getBoolean(Const.OK);
			id = json.getString(Const.ID);
		}

		if (!ok) {
			Logger.error(TAG + "createUser", "error in creating user");
		}

		return id;
	}

	/**
	 * Update user response object, the Const.REV value is important in order to
	 * continue using the application
	 * 
	 * If you are updating contacts or favorites on of them should be null
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static boolean updateUser(JSONObject json, List<String> contactsIds,
			List<String> groupsIds) throws JSONException {

		String rev = "";

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return false;
			}

			rev = json.getString(Const._REV);

			UsersManagement.getLoginUser().setRev(rev);

			if (null != contactsIds) {
				UsersManagement.getLoginUser().setContactIds(
						contactsIds);
			}

			if (null != groupsIds) {
				UsersManagement.getLoginUser().setGroupIds(groupsIds);
			}

			return true;
		}

		return false;
	}

	/**
	 * JSON response from creating a group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static String createGroup(JSONObject json) throws JSONException {

		boolean ok = false;
		String id = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return null;
			}

			ok = json.getInt(Const.OK) == 1;
			id = json.getString(Const.ID);
		}

		if (!ok) {
			Logger.error(TAG + "createGroup", "error in creating a group");
			return null;
		}

		return id;
	}

	/**
	 * JSON response from deleting a group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static boolean deleteGroup(JSONObject json) throws JSONException {

		boolean ok = false;

		if (json != null) {

		    if (json.has(Const.ERROR)) {
				return false;
			}
			
			ok = json.getBoolean(Const.OK);
		}

		return ok;
	}

	public static String findAvatarFileId(JSONObject json) throws JSONException {
		String avatarFileId = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return null;
			}

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) 
			{
				JSONObject row = rows.getJSONObject(i);
				avatarFileId = row.getString(Const.VALUE);
			}
		}

		return avatarFileId;
	}

	/**
	 * JSON response from deleting a user group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static boolean deleteUserGroup(JSONObject json) throws JSONException {

		boolean ok = false;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return false;
			}

			ok = json.getBoolean(Const.OK);
		}

		return ok;
	}

	/**
	 * JSON response from creating a user group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static String createUserGroup(JSONObject json) throws JSONException {

		boolean ok = false;
		String id = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return null;
			}

			ok = json.getBoolean(Const.OK);
			id = json.getString(Const.ID);
		}

		if (!ok) {
			Logger.error(TAG + "createUserGroup", "error in creating a group");
			return null;
		}

		return id;
	}

	/**
	 * JSON response from updating a group you own
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static boolean updateGroup(JSONObject json) throws JSONException {

		boolean ok = false;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return false;
			}

			
			ok = json.getInt(Const.OK) == 1;

			/* Important */
			UsersManagement.getToGroup().setRev(json.getString(Const.REV));
		}

		if (!ok) {
			Logger.error(TAG + "updateGroup", "error in updating a group");
		}

		return ok;
	}

	/**
	 * Parse single JSON object of type Group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static Group parseSingleGroupObject(JSONObject json) throws JSONException {

		Group group = null;

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				return null;
			}

			JSONArray rows = json.getJSONArray(Const.ROWS);
			JSONObject row = rows.getJSONObject(0);

			JSONObject groupJson = row.getJSONObject(Const.VALUE);
			group = sGsonExpose.fromJson(groupJson.toString(), Group.class);
		}

		return group;
	}

	   /**
     * Parse single JSON object of type Group
     * 
     * @param json
     * @return
     */
    public static Group parseSingleGroupObjectWithoutRowParam(JSONObject json) {

        Group group = null;

        if (json != null) {
        	
        	if (json.has(Const.ERROR)) {
				return null;
			}

        	if (json.length() == 0) {
        		return null;
        	}
        	
        	if (json.has(Const.NAME)) {
        		group = sGsonExpose.fromJson(json.toString(), Group.class);   
        	}            
        }

        return group;
    }
    
	/**
	 * Parse multi JSON objects of type Group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Group> parseMultiGroupObjects(JSONObject json) throws JSONException {

		List<Group> groups = new ArrayList<>();

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return groups;
			}

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				String key = row.getString(Const.KEY);

				if (!key.equals(Const.NULL)) {

					JSONObject groupJson = row.getJSONObject(Const.VALUE);

					Group group = sGsonExpose.fromJson(
							groupJson.toString(), Group.class);

					groups.add(group);
				}
			}
		}

		return groups;
	}

	/**
	 * Parse favorite groups JSON objects
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Group> parseFavoriteGroups(JSONObject json) throws JSONException {

		List<Group> groups = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return null;
			}
			
			groups = new ArrayList<Group>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);

				JSONObject groupJson = row.getJSONObject(Const.DOC);

				String type = groupJson.getString(Const.TYPE);
				if (!type.equals(Const.GROUP)) {
					continue;
				}

				Group group = sGsonExpose.fromJson(
						groupJson.toString(), Group.class);

				groups.add(group);	
			}
		}

		return groups;
	}

	/**
	 * Parse multi JSON objects of type UserGroup
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<UserGroup> parseMultiUserGroupObjects(JSONObject json) throws JSONException {

		List<UserGroup> usersGroup = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return null;
			}

			usersGroup = new ArrayList<UserGroup>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				String key = row.getString(Const.KEY);

				if (!key.equals(Const.NULL)) {

					JSONObject userGroupJson = row
							.getJSONObject(Const.VALUE);

					UserGroup userGroup = sGsonExpose.fromJson(
							userGroupJson.toString(), UserGroup.class);
					usersGroup.add(userGroup);
				}
			}
			
		}

		return usersGroup;
	}
	
	/**
	 * Parse multi JSON objects of type GroupCategory
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<GroupCategory> parseMultiGroupCategoryObjects(JSONObject json) throws JSONException {
		List<GroupCategory> groupCategories = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return null;
			}

			groupCategories = new ArrayList<GroupCategory>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				String key = row.getString(Const.KEY);

				if (!key.equals(Const.NULL)) {

					JSONObject groupCategoryJson = row.getJSONObject(Const.VALUE);

					GroupCategory groupCategory = sGsonExpose.fromJson(
							groupCategoryJson.toString(), GroupCategory.class);

					if (groupCategoryJson.has(Const.ATTACHMENTS)) {
						List<Attachment> attachments = new ArrayList<Attachment>();

						JSONObject json_attachments = groupCategoryJson
								.getJSONObject(Const.ATTACHMENTS);

						@SuppressWarnings("unchecked")
						Iterator<String> keys = json_attachments.keys();
						while (keys.hasNext()) {
							String attachmentKey = keys.next();
							try {
								JSONObject json_attachment = json_attachments
										.getJSONObject(attachmentKey);
								Attachment attachment = sGsonExpose
										.fromJson(
												json_attachment.toString(),
												Attachment.class);
								attachment.setName(attachmentKey);
								attachments.add(attachment);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						groupCategory.setAttachments(attachments);	
					}

					groupCategories.add(groupCategory);
				}
			}
		}

		return groupCategories;
	}
	
	public static List<Member> parseMemberObjects(JSONObject jsonData) throws JSONException {
		// Get total member
		int totalMember = jsonData.getInt("count");
		Member.setTotalMember(totalMember);
		
		// Get list member
		List<Member> listMembers = null;
		JSONArray jsonArray = jsonData.getJSONArray("users");
		if (jsonArray != null) {
			listMembers = new ArrayList<Member>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject row = jsonArray.getJSONObject(i);
				try {
					String id = row.getString("_id");
					String name = row.getString("name");
					String image = row.getString("avatar_thumb_file_id");
					String online = row.getString("online_status");
					Member member = new Member(id, name, image, online);
					listMembers.add(member);
				} catch (JSONException e) {
				}
			}
		}

		return listMembers;
	}

	/**
	 * Find a single Message object
	 * 
	 * @param json
	 * @return
	 * @throws TogathorException
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static Message findMessage(JSONObject json) {
		
		if (json.has(Const.ERROR)) {
			return null;
		}
		
		return parseMessageObject(json, false, false, false);
	}

	/**
	 * Find all messages for current user
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 * @throws TogathorException
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static ArrayList<Message> findMessagesForUser(JSONObject json) throws JSONException {
		ArrayList<Message> messages = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				return null;
			}

			messages = new ArrayList<Message>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				JSONObject msgJson = row.getJSONObject(Const.VALUE);

				Message message = null;

				String messageType = msgJson
						.getString(Const.MESSAGE_TYPE);

				if (messageType.equals(Const.TEXT)) {

					message = new Gson().fromJson(msgJson.toString(),
							Message.class);

				} else if (messageType.equals(Const.IMAGE)) {

					message = parseMessageObject(msgJson, true, false,
							false);

				} else if (messageType.equals(Const.VOICE)) {

					message = parseMessageObject(msgJson, false, true,
							false);

				} else if (messageType.equals(Const.VIDEO)) {

					message = parseMessageObject(msgJson, false, false,
							true);
				}
				else if (messageType.equals(Const.EMOTICON)) {

					message = parseMessageObject(msgJson, false, false,
							false);
				} else {

					message = new Gson().fromJson(msgJson.toString(),
							Message.class);

				}

				if (message == null) 
				{
                }
				else 
				{
					messages.add(message);
				}
			}
		}

		if (null != messages) {
			Collections.sort(messages);
		}

		return messages;
	}

	/**
	 * Parse a single JSON object of Message type
	 * 
	 * @param json
	 * @param image
	 * @param voice
	 * @return
	 * @throws TogathorException
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private static Message parseMessageObject(JSONObject json, boolean image,
			boolean voice, boolean video) {

		Message message = new Message();

		if (json == null) {
			return message;
		}

		if (json.has(Const.ERROR)) {
			return null;
		}

		try {
			message.setId(json.getString(Const._ID));
		} catch (JSONException e) {
			message.setId("");
		}

		try {
			message.setRev(json.getString(Const._REV));
		} catch (JSONException e) {
			message.setRev("");
		}

		try {
			message.setType(json.getString(Const.TYPE));
		} catch (JSONException e) {
			message.setType("");
		}

		try {
			message.setMessageType(json.getString(Const.MESSAGE_TYPE));
		} catch (JSONException e) {
			message.setMessageType("");
		}

		try {
			message.setMessageTargetType(json
					.getString(Const.MESSAGE_TARGET_TYPE));
		} catch (JSONException e) {
			message.setMessageTargetType("");
		}

		try {
			message.setBody(json.getString(Const.BODY));
		} catch (JSONException e) {
			message.setBody("");
		}

		try {
			message.setFromUserId(json.getString(Const.FROM_USER_ID));
		} catch (JSONException e) {
			message.setFromUserId("");
		}

		try {
			message.setFromUserName(json.getString(Const.FROM_USER_NAME));
		} catch (JSONException e) {
			message.setFromUserName("");
		}

		try {
			message.setToUserId(json.getString(Const.TO_USER_ID));
		} catch (JSONException e) {
			message.setToUserId("");
		}

		try {
			message.setToGroupName(json.getString(Const.TO_USER_NAME));
		} catch (JSONException e) {
			message.setToGroupName("");
		}

		try {
			message.setToGroupId(json.getString(Const.TO_GROUP_ID));
		} catch (JSONException e) {
			message.setToGroupId("");
		}

		try {
			message.setToGroupName(json.getString(Const.TO_GROUP_NAME));
		} catch (JSONException e) {
			message.setToGroupName("");
		}

		try {
			message.setCreated(json.getLong(Const.CREATED));
		} catch (JSONException e) {
			return null;
		}

		try {
			message.setModified(json.getLong(Const.MODIFIED));
		} catch (JSONException e) {
			return null;
		}

		try {
			message.setValid(json.getBoolean(Const.VALID));
		} catch (JSONException e) {
			message.setValid(true);
		}

		try {
			message.setAttachments(json.getJSONObject(Const.ATTACHMENTS)
					.toString());
		} catch (JSONException e) {
			message.setAttachments("");
		}

		try {
			message.setLatitude(json.getString(Const.LATITUDE));
		} catch (JSONException e) {
			message.setLatitude("");
		}

		try {
			message.setLongitude(json.getString(Const.LONGITUDE));
		} catch (JSONException e) {
			message.setLongitude("");
		}

		try {
			message.setImageFileId((json.getString(Const.PICTURE_FILE_ID)));
		} catch (JSONException e) {
			message.setImageFileId("");
		}

       try {
                message.setImageThumbFileId((json.getString(Const.PICTURE_THUMB_FILE_ID)));
        } catch (JSONException e) {
                message.setImageThumbFileId("");
        }
	      
		try {
			message.setVideoFileId((json.getString(Const.VIDEO_FILE_ID)));
		} catch (JSONException e) {
			message.setVideoFileId("");
		}

		try {
			message.setVoiceFileId((json.getString(Const.VOICE_FILE_ID)));
		} catch (JSONException e) {
			message.setVoiceFileId("");
		}

		try {
			message.setEmoticonImageUrl(json
					.getString(Const.EMOTICON_IMAGE_URL));
		} catch (JSONException e) {
			message.setEmoticonImageUrl("");
		}

		try {
			message.setAvatarFileId(json.getString(Const.AVATAR_THUMB_FILE_ID));
		} catch (JSONException e) {
			message.setAvatarFileId("");
		}
		
		try {
			message.setDeleteType(json.getInt(Const.DELETE_TYPE));
		} catch (JSONException e) {
			message.setDeleteType(0);
		}
		
		try {
			message.setDelete(json.getInt(Const.DELETE_AT));
		} catch (JSONException e) {
			message.setDelete(0);
		}
		
		try {
			message.setReadAt(json.getInt(Const.READ_AT));
		} catch (JSONException e) {
			message.setReadAt(0);
		}
		
		
		try {
			message.setCommentCount(json.getInt(Const.COMMENT_COUNT));
		} catch (JSONException e) {
			message.setCommentCount(0);
		}
		
		
//		if (image || video || voice) {
//			message.setCommentCount(CouchDB.getCommentCount(message.getId()));
//		}

		return message;
	}
	
	private static boolean isInvalidToken(JSONObject json) {
		if (json.has(Const.MESSAGE)) {
			try {
				String errorMessage = json.getString(Const.MESSAGE);
				if (errorMessage.equalsIgnoreCase(Const.INVALID_TOKEN)) {
					return true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Parse server objects
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Server> parseServers(JSONArray json) throws JSONException {

		List<Server> servers = null;

		if (json != null) {
			
			servers = new ArrayList<Server>();

			for (int i = 0; i < json.length(); i++) {

				JSONObject row = json.getJSONObject(i);
				
				servers.add(sGsonExpose.fromJson(row.toString(), Server.class));

			}
		} else{
			return null;
		}

		return servers;
	}
	

}
