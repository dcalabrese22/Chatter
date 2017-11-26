package com.dcalabrese22.dan.chatter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by dan on 9/14/17.
 */

//custom viewholder for a single chat message
public class ChatViewHolder extends RecyclerView.ViewHolder {

    private TextView mChatBody;

    public ChatViewHolder(View view) {
        super(view);
        mChatBody = view.findViewById(R.id.tv_chat_body);

    }

    public void setChatBody(String body) {
        mChatBody.setText(body);
    }
}
