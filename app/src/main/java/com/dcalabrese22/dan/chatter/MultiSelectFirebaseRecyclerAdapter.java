package com.dcalabrese22.dan.chatter;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import com.dcalabrese22.dan.chatter.Objects.Conversation;
import com.dcalabrese22.dan.chatter.Objects.SelectedConversation;
import com.dcalabrese22.dan.chatter.interfaces.MessageExtrasListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by dan on 10/8/17.
 */

public class MultiSelectFirebaseRecyclerAdapter extends FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> {

    private ProgressBar mBar;

    public MultiSelectFirebaseRecyclerAdapter(Class model, int layout,
                                              Class viewHolder, DatabaseReference reference,
                                              ProgressBar bar) {
        super(model, layout, viewHolder, reference);
        mBar = bar;
    }

    @Override
    protected void populateViewHolder(ConversationViewHolder viewHolder, Conversation model, int position) {

        viewHolder.setLastMessage(model.getLastMessage());

    }



//    @Override
//    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        ConversationViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
//        viewHolder.setOnClickListener(new ConversationClickListener() {
//            @Override
//            public void onConversationClick(View view, int position) {
//                Conversation model = getItem(position);
//                String id = model.getId();
//                String user = model.getUser();
//                mListener.getMessageUser(user);
//                mListener.sendMessageId(id);
//
//            }
//        });
//
//        return viewHolder;
//    }
//
//
//    @Override
//    public void onBindViewHolder(ConversationViewHolder viewHolder, int position) {
//        if (mSelectedConversations.contains(getItem(position))) {
//            viewHolder.mLinearLayout.setBackgroundColor(ContextCompat
//                    .getColor(mContext, R.color.selected_background));
//        }
//    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        mBar.setVisibility(View.INVISIBLE);
    }




}
