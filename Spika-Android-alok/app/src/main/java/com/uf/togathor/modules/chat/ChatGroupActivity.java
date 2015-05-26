package com.uf.togathor.modules.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.view.MaterialListView;
import com.gc.materialdesign.views.Button;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenVideo;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.kbeanie.imagechooser.api.VideoChooserListener;
import com.kbeanie.imagechooser.api.VideoChooserManager;
import com.uf.togathor.modules.attendance.AttendanceActivity;
import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.Command;
import com.uf.togathor.db.couchdb.CouchDB;
import com.uf.togathor.db.couchdb.ResultListener;
import com.uf.togathor.db.couchdb.TogathorAsyncTask;
import com.uf.togathor.db.couchdb.TogathorException;
import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.model.Message;
import com.uf.togathor.uitems.Animations;
import com.uf.togathor.uitems.cards.CustomImageCard;
import com.uf.togathor.uitems.cards.CustomTextCard;
import com.uf.togathor.utils.constants.Const;
import com.uf.togathor.utils.Utils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Alok on 12/17/2014.
 */
public class ChatGroupActivity extends ActionBarActivity implements ImageView.OnClickListener, ImageChooserListener, VideoChooserListener {

    private static final String TAG = "GroupChatActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    enum ToolbarState {
        EXPANDED,
        CLOSED
    }

    private EditText mOutEditText;
    private ImageView mSendButton;
    private Toolbar toolbar;
    private ImageView expandGroup;
    private ImageView buttonAttach;
    private ImageView chooseImage;
    private ImageView chooseCamera;
    private ImageView chooseVideo;
    private TextView toGroupTV;
    private Animations animations = new Animations();
    private RelativeLayout toolbarContainer;
    private CardView mediaChooser;
    private ToolbarState toolbarState;
    private Button attendance;

    private ImageChooserManager imageChooserManager;
    private VideoChooserManager videoChooserManager;
    private int chooserType;
    private String filePath;

    private Group toGroup;
    private User fromUser;

    public static ChatGroupActivity sInstance = null;
    private Message message;
    private Card card;

    public static MaterialListView chatListView;
    public static ArrayList<Message> gCurrentMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;
        setContentView(R.layout.activity_chat_group);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        expandGroup = (ImageView) findViewById(R.id.expand_user);
        expandGroup.setOnClickListener(this);
        toolbarContainer = (RelativeLayout) findViewById(R.id.toolbar_container);
        buttonAttach = (ImageView) findViewById(R.id.button_attach);
        mediaChooser = (CardView) findViewById(R.id.choose_media);
        chooseCamera = (ImageView) findViewById(R.id.choose_camera);
        chooseImage = (ImageView) findViewById(R.id.choose_image);
        chooseVideo = (ImageView) findViewById(R.id.choose_video);
        attendance = (Button) findViewById(R.id.attendance);
        mediaChooser.setVisibility(View.GONE);

        buttonAttach.setOnClickListener(new AttachmentClickListener(Const.DATA));
        chooseCamera.setOnClickListener(new AttachmentClickListener(Const.IMAGE + "c"));
        chooseImage.setOnClickListener(new AttachmentClickListener(Const.IMAGE));
        chooseVideo.setOnClickListener(new AttachmentClickListener(Const.VIDEO));

        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatGroupActivity.this, AttendanceActivity.class);
                intent.putExtra("course_name", toGroup.getName());
                intent.putExtra("course_id", toGroup.getId());
                intent.putExtra("course_instructor", toGroup.getName());
                startActivity(intent);
            }
        });

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbarState = ToolbarState.CLOSED;
            toolbar.setBackgroundColor(Color.parseColor("#2196F3"));
            setTitle("");
        }

        fromUser = UsersManagement.getFromUser();
        toGroup = UsersManagement.getToGroup();
        UsersManagement.setToUser(null);
        toGroupTV = (TextView) findViewById(R.id.to_group);
        toGroupTV.setText(toGroup.getName());

        Log.d(TAG, "Group type " + toGroup.getCategoryId());

        setupChat();
        refreshWallMessages();
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mSendButton = (ImageView) findViewById(R.id.button_send);
        chatListView = (MaterialListView) findViewById(R.id.chat_view);
        chatListView.setCardAnimation(MaterialListView.CardAnimation.SWING_RIGHT_IN);
        gCurrentMessages = new ArrayList<>();

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = mOutEditText.getText().toString();
                sendMessage(message, Const.TEXT);
            }
        });

        Togathor.getMessagesDataSource().close();
        Togathor.getMessagesDataSource().open();

    }

    @Override
    public void onBackPressed() {
        Togathor.getMessagesDataSource().close();
        UsersManagement.setToGroup(null);
        super.onBackPressed();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_group, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message, Const.TEXT);
            }
            return true;
        }
    };

    private void sendMessage(String messageBody, String type) {

        long created = System.currentTimeMillis() / 1000;

        if (mOutEditText.getText().toString().equals("") && type.equals(Const.TEXT))
            return;

        switch (type) {

            case Const.TEXT:
                message = new Message();
                message.setId(Const._ID);
                message.setBody(messageBody);
                message.setFromUserId(UsersManagement.getLoginUser().getId());
                message.setFromUserName(UsersManagement.getLoginUser().getName());
                message.setToGroupId(toGroup.getId());
                message.setToGroupName(toGroup.getName());
                message.setMessageType(Const.TEXT);
                message.setCreated(created);
                message.setModified(created);

                SyncModule.sendMessage(message, this, null, UsersManagement.getToGroup());
                updateListView(message);
                mOutEditText.setText("");
                break;

            case Const.IMAGE:
                fileUploadAsync(messageBody, Const.IMAGE);
                break;

            case Const.VIDEO:
                fileUploadAsync(messageBody, Const.VIDEO);
                break;
        }
    }

    private void fileUploadAsync(String filePath, String fileType) {
        new TogathorAsyncTask<Void, Void, ArrayList<String>>(new FileUpload(filePath, fileType), new ImageUploadFinished(), ChatGroupActivity.this, true).execute();
    }

    private class FileUpload implements Command<ArrayList<String>> {

        String filePath;
        String fileType;

        public FileUpload(String filePath, String fileType) {
            this.filePath = filePath;
            this.fileType = fileType;
        }

        @Override
        public ArrayList<String> execute() throws JSONException, IOException,
                TogathorException {

            long created = System.currentTimeMillis() / 1000;

            switch (fileType) {

                case Const.IMAGE:
                    String fileId = CouchDB.uploadFile(filePath);
                    String tmppath = getExternalCacheDir() + "/" + Const.TMP_BITMAP_FILENAME;
                    Bitmap originalBitmap = BitmapFactory.decodeFile(filePath);
                    Bitmap thumbBitmap = Utils.scaleBitmap(originalBitmap, Const.PICTURE_THUMB_SIZE, Const.PICTURE_THUMB_SIZE);
                    Utils.saveBitmapToFile(thumbBitmap, tmppath);
                    String thumbFileId = CouchDB.uploadFile(tmppath);
                    ArrayList<String> list = new ArrayList<>();
                    list.add(fileId);
                    list.add(thumbFileId);

                    message = new Message();
                    message.setId(Const._ID);
                    message.setImageFileId(fileId);
                    message.setImageLocalFileId(null);
                    message.setImageThumbFileId(thumbFileId);
                    message.setImageThumbLocalFileId(null);
                    message.setFromUserId(UsersManagement.getLoginUser().getId());
                    message.setFromUserName(UsersManagement.getLoginUser().getName());
                    message.setToGroupId(toGroup.getId());
                    message.setToGroupName(toGroup.getName());
                    message.setMessageType(Const.IMAGE);
                    message.setCreated(created);
                    message.setModified(created);
                    return list;

                case Const.VIDEO:
                    CouchDB.uploadFileAsync(filePath, new VideoUploadFinished(), ChatGroupActivity.this, true);
                    return new ArrayList<>();

                default:
                    return null;
            }
        }
    }

    private String generateSequence() {
        return UUID.randomUUID().toString();
    }

    private class ImageUploadFinished implements ResultListener<ArrayList<String>> {

        @Override
        public void onResultsSucceeded(ArrayList<String> result) {

            if (result.size() == 0)
                return;

            SyncModule.sendMessage(message, sInstance, null, UsersManagement.getToGroup());

            //TODO
            updateListView(message);
            mOutEditText.setText("");
        }

        @Override
        public void onResultsFail() {
        }
    }

    private class VideoUploadFinished implements ResultListener<String> {

        @Override
        public void onResultsSucceeded(String result) {

            long created = System.currentTimeMillis() / 1000;
            message.setVideoFileId(result);

            SyncModule.sendMessage(message, sInstance, null, UsersManagement.getToGroup());

            //TODO
            //mConversationView.setSelection(mConversationArrayAnimationAdapter.getCount() - 1);
            mOutEditText.setText("");

        }

        @Override
        public void onResultsFail() {
        }
    }

    private void refreshWallMessages() {

        ArrayList<Message> existingMessages = Togathor.getMessagesDataSource().getAllMessages(null, toGroup);

        for (Message message : existingMessages) {
            updateListView(message);
        }
        chatListView.setSelection(chatListView.getCount() - 1);
    }

    private void updateListView(Message message) {

        switch (message.getMessageType()) {
            case Const.TEXT:
                card = new CustomTextCard(R.layout.card_text_message, message);
                break;

            case Const.IMAGE:
                card = new CustomImageCard(R.layout.card_image_message, message);
                break;
        }

        gCurrentMessages.add(message);
        if(!message.getMessageType().equals(Const.LOCATION)) {
            chatListView.add(card);
            chatListView.setSelection(chatListView.getCount() - 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_delete_exit_from_group:
                Utils.deleteGroup(ChatGroupActivity.this, toGroup, true);
                return true;
            case R.id.menu_view_group_member:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Toolbar Expand/Contract animation
    @Override
    public void onClick(View v) {

        animations.setupScene(toolbar);

        if (toolbarState == ToolbarState.CLOSED) {
            animations.springExpand();
            toolbarState = ToolbarState.EXPANDED;
            expandGroup.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_less_white_36dp));
        } else {
            animations.springClose();
            toolbarState = ToolbarState.CLOSED;
            expandGroup.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_more_white_36dp));
        }

    }

    private class AttachmentClickListener implements View.OnClickListener {

        String type;

        private AttachmentClickListener(String type) {
            this.type = type;
        }

        @Override
        public void onClick(View v) {

            switch (type) {
                case Const.IMAGE:
                    chooseImage();
                    break;

                case Const.IMAGE + "c":
                    chooseCamera();
                    break;

                case Const.VIDEO:
                    //chooseVideo();
                    break;

                default:
                    if (mediaChooser.getVisibility() == View.VISIBLE)
                        mediaChooser.setVisibility(View.GONE);
                    else
                        mediaChooser.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void chooseImage() {
        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, "ToGathor", true);
        imageChooserManager.setImageChooserListener(this);
        try {
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chooseCamera() {
        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_CAPTURE_PICTURE, "ToGathor", true);
        imageChooserManager.setImageChooserListener(this);
        try {
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chooseVideo() {
        chooserType = ChooserType.REQUEST_PICK_VIDEO;
        videoChooserManager = new VideoChooserManager(this,
                ChooserType.REQUEST_PICK_VIDEO);
        videoChooserManager.setVideoChooserListener(this);
        try {
            videoChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String videolocalid;
        long created = System.currentTimeMillis() / 1000;

        if (resultCode == RESULT_OK
                && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        } else if (resultCode == RESULT_OK
                && (requestCode == ChooserType.REQUEST_CAPTURE_VIDEO || requestCode == ChooserType.REQUEST_PICK_VIDEO)) {
            if (videoChooserManager == null) {
                reinitializeVideoChooser();
            }
            videoChooserManager.submit(requestCode, data);
        } else if (resultCode == RESULT_OK && requestCode == 2048) {
            videolocalid = data.getStringExtra("videolocalid");

            message = new Message();
            message.setId(Const._ID);
            message.setVideoLocalFileId(videolocalid);
            message.setFromUserId(UsersManagement.getLoginUser().getId());
            message.setFromUserName(UsersManagement.getLoginUser().getName());
            message.setToGroupId(toGroup.getId());
            message.setToGroupName(toGroup.getName());
            message.setMessageType(Const.VIDEO);
            message.setCreated(created);
            message.setModified(created);
            sendMessage(videolocalid, Const.VIDEO);
        } else if (resultCode == RESULT_OK && requestCode == Const.CROPPED_IMAGE_REQUEST) {
            if (!data.getStringExtra("image_file").isEmpty())
                sendMessage(data.getStringExtra("image_file"), Const.IMAGE);
        }
    }

    @Override
    public void onImageChosen(final ChosenImage image) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (image != null) {
                    Log.d("ImageChoose", image.getFilePathOriginal());

                    startActivityForResult(new Intent(ChatGroupActivity.this,
                            ImageCropActivity.class).putExtra("image_file", image.getFilePathOriginal()), Const.CROPPED_IMAGE_REQUEST);
                }
            }
        });

    }

    @Override
    public void onError(String s) {

    }

    @Override
    public void onVideoChosen(final ChosenVideo chosenVideo) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (chosenVideo != null) {

                }
            }
        });
    }

    // Should be called if for some reason the ImageChooserManager is null (Due
    // to destroying of activity for low memory situations)
    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManager(this, chooserType,
                "ToGathor", true);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    private void reinitializeVideoChooser() {
        videoChooserManager = new VideoChooserManager(this, chooserType,
                "ToGathor", true);
        videoChooserManager.setVideoChooserListener(this);
        videoChooserManager.reinitialize(filePath);
    }

    public void showImages(Message currentMessage) {

        mediaChooser.setVisibility(View.GONE);

        ArrayList<String> images = new ArrayList<>();
        int cnt = 0, finalPosition = 0;

        for (Message message : gCurrentMessages) {
            if (message.getMessageType().equals(Const.IMAGE)) {
                if (message.getImageFileId().equals(currentMessage.getImageFileId())) {
                    finalPosition = cnt;
                }
                images.add(message.getImageFileId());
                cnt++;
            }
        }

        Intent imageIntent = new Intent(this, ImageViewerActivity.class);
        imageIntent.putStringArrayListExtra("images", images);
        imageIntent.putExtra("currentimage", finalPosition);
        startActivity(imageIntent);
    }

    public void updateMessageDB(Message message) {
        Togathor.getMessagesDataSource().replaceMessage(message, toGroup);
    }
}
