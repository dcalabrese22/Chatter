package com.dcalabrese22.dan.chatter.Objects;

import android.view.View;

import com.dcalabrese22.dan.chatter.ConversationViewHolder;

/**
 * Created by dcalabrese on 10/9/2017.
 */

//custom object that represents a selected conversation
public class SelectedConversation {

    private View mView;
    private ConversationViewHolder mViewHolder;
    private int mPosition;
    private Conversation mConversation;

    public SelectedConversation(View view, ConversationViewHolder viewHolder,
                                int position, Conversation conversation) {
        mView = view;
        mViewHolder = viewHolder;
        mPosition = position;
        mConversation = conversation;
    }

    public View getSelectedView() {
        return mView;
    }

    public ConversationViewHolder getViewHolder() {
        return mViewHolder;
    }

    public int getPosition() {
        return mPosition;
    }

    public Conversation getConversation() {
        return mConversation;
    }

    @Override
    public String toString() {
        return mConversation.toString();
    }
}
