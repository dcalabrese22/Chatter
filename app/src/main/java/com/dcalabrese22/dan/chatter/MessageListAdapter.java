package com.dcalabrese22.dan.chatter;

import android.content.Context;

import com.dcalabrese22.dan.chatter.Objects.Conversation;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

/**
 * Created by dcalabrese on 10/5/2017.
 */

public class MessageListAdapter extends FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> {

    private Class<Conversation> mPbConversationClass;
    private int mLayout;
    private Class<ConversationViewHolder> mConversationViewHolderCLass;
    private Query mQuery;
    private Context mContext;
    private int mSelectedPosition;

    public MessageListAdapter(Class<Conversation> pbConversationClass, int layout,
                              Class<ConversationViewHolder> conversationViewHolderClass,
                              Query query, Context context) {
        super(pbConversationClass, layout, conversationViewHolderClass, query);
        mPbConversationClass = pbConversationClass;
        mLayout = layout;
        mConversationViewHolderCLass = conversationViewHolderClass;
        mQuery = query;
        mContext = context;
    }


    @Override
    protected void populateViewHolder(ConversationViewHolder viewHolder, Conversation model, int position) {
        viewHolder.setSubject(model.getTitle());

        viewHolder.setLastMessage(model.getLastMessage());

    }

}
