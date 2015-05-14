package com.uf.togathor.uitems.cards;

import android.content.Context;
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
import com.uf.togathor.modules.chat.ChatGroupActivity;
import com.uf.togathor.modules.chat.ChatUserActivity;
import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.model.Message;
import com.uf.togathor.utils.constants.Const;

/**
 * Created by Alok on 3/10/2015
 */
public class CustomImageView extends CardItemView<CustomImageCard> implements View.OnClickListener  {

    ImageView messageView;
    CardView fromCardView;
    CardView toCardView;
    TextView toName;
    Context context;
    Message message;
    String loginUserID;
    CustomImageView instance;

    ProgressBarCircularIndeterminate progress;

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CustomImageView(Context context) {
        super(context);
        this.context = context;
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public void build(CustomImageCard customImageCard) {

        instance = this;
        this.setOnClickListener(this);
        fromCardView = (CardView) findViewById(R.id.from_image_card_view);
        toCardView = (CardView) findViewById(R.id.to_image_card_view);
        toName = (TextView) findViewById(R.id.to_name);

        fromCardView.setVisibility(GONE);
        toCardView.setVisibility(GONE);

        if(UsersManagement.getLoginUser() != null)
            loginUserID = UsersManagement.getLoginUser().getId();
        else
            loginUserID = Togathor.getPreferences().getUserId();

        message = customImageCard.getMessage();

        if(message.getFromUserId().equals(loginUserID))  {
            fromCardView.setVisibility(VISIBLE);
            progress = (ProgressBarCircularIndeterminate) findViewById(R.id.from_progress_bar);
            messageView = (ImageView) findViewById(R.id.from_image_content);
        }
        else {
            toCardView.setVisibility(VISIBLE);

            if(!message.getToGroupId().equals("0")) {
                toName.setVisibility(VISIBLE);
                toName.setText(message.getFromUserName());
            }

            progress = (ProgressBarCircularIndeterminate) findViewById(R.id.to_progress_bar);
            messageView = (ImageView) findViewById(R.id.to_image_content);
        }

        progress.setVisibility(VISIBLE);

        Glide.with(context)
                .load(Togathor.getInstance().getBaseUrlWithSufix(
                Const.FILE_DOWNLOADER_URL) + Const.FILE + "=" + message.getImageThumbFileId())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progress.setVisibility(GONE);
                        return false;
                    }
                })
                .centerCrop()
                .into(messageView);
    }

    @Override
    public void onClick(View v) {
        if(message.getToGroupId() == null || message.getToGroupId().equals("0"))
            ((ChatUserActivity) context).showImages(message);
        else
            ((ChatGroupActivity) context).showImages(message);
    }
}
