package com.uf.togathor.uitems.cards;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.TextView;

import com.dexafree.materialList.model.CardItemView;
import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.model.Message;

/**
 * Created by Alok on 3/10/2015
 */
public class CustomTextView extends CardItemView<CustomTextCard> {

    TextView messageView;
    TextView toName;
    CardView fromCardView;
    CardView toCardView;
    String loginUserID;

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(CustomTextCard customTextCard) {

        Message message = customTextCard.getMessage();

        fromCardView = (CardView) findViewById(R.id.from_text_card_view);
        toCardView = (CardView) findViewById(R.id.to_text_card_view);

        toName = (TextView) findViewById(R.id.to_name);

        fromCardView.setVisibility(GONE);
        toCardView.setVisibility(GONE);

        if(UsersManagement.getLoginUser() != null)
            loginUserID = UsersManagement.getLoginUser().getId();
        else
            loginUserID = Togathor.getPreferences().getUserId();

        if(message.getFromUserId().equals(loginUserID))  {
            fromCardView.setVisibility(VISIBLE);
            messageView = (TextView) findViewById(R.id.from_text_content);
        }
        else {
            toCardView.setVisibility(VISIBLE);
            messageView = (TextView) findViewById(R.id.to_text_content);
            if(message.getToGroupId() != null && !message.getToGroupId().equals("0")) {
                toName.setVisibility(VISIBLE);
                toName.setText(message.getFromUserName());
            }
        }
        messageView.setText(customTextCard.getMessage().getBody());
    }
}
