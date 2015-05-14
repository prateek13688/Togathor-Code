package com.uf.togathor.uitems.cards;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dexafree.materialList.model.CardItemView;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.model.Message;
import com.uf.togathor.modules.chat.ChatGroupActivity;
import com.uf.togathor.modules.chat.ChatUserActivity;
import com.uf.togathor.uitems.LetterAvatar;
import com.uf.togathor.utils.constants.Const;

import java.util.Random;

/**
 * Created by Alok on 3/10/2015
 */
public class CustomUserView extends CardItemView<CustomUserCard> implements View.OnClickListener  {

    ImageView userDP;
    TextView userName;
    ImageView userStatus;
    User user;
    Context context;
    CustomUserView instance;
    String loginUserID;

    public CustomUserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CustomUserView(Context context) {
        super(context);
        this.context = context;
    }

    public CustomUserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public void build(CustomUserCard customUserCard) {

        instance = this;
        this.setOnClickListener(this);
        userName = (TextView) findViewById(R.id.user_name);
        userDP = (ImageView) findViewById(R.id.user_dp);

        if(UsersManagement.getLoginUser() != null)
            loginUserID = UsersManagement.getLoginUser().getId();
        else
            loginUserID = Togathor.getPreferences().getUserId();

        user = customUserCard.getUser();
        userName.setText(user.getName());

        /*Glide.with(context)
                .load(Togathor.getInstance().getBaseUrlWithSufix(
                Const.FILE_DOWNLOADER_URL) + Const.FILE + "=" + user.getAvatarThumbFileId())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .centerCrop()
                .into(userDP);*/

        userDP.setImageDrawable(new LetterAvatar(context, getResources().getColor(R.color.accent1),
                user.getName().charAt(0) + "", 32));
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        UsersManagement.setToUser(user);
        UsersManagement.setToGroup(null);

        if (user != null) {
            intent = new Intent(context, ChatUserActivity.class);
            (context).startActivity(intent);
        }
    }
}
