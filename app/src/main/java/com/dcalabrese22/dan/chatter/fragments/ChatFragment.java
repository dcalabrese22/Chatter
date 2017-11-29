package com.dcalabrese22.dan.chatter.fragments;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.dcalabrese22.dan.chatter.AppWidget;
import com.dcalabrese22.dan.chatter.ChatViewHolder;
import com.dcalabrese22.dan.chatter.MainActivity;
import com.dcalabrese22.dan.chatter.Objects.ChatMessage;
import com.dcalabrese22.dan.chatter.Objects.Notification;
import com.dcalabrese22.dan.chatter.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//Fragment for showing a chat between 2 users
public class ChatFragment extends Fragment {

    private String mMessagePushKey;
    private String mUserName;
    private String mUser2Name;
    private Context mContext;
    private FirebaseRecyclerAdapter<ChatMessage, RecyclerView.ViewHolder> mAdapter;


    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        mMessagePushKey = getArguments().getString(MainActivity.MESSAGE_PUSH_KEY);
        mUserName = getArguments().getString(MainActivity.USER_NAME);
        mUser2Name = getArguments().getString(MainActivity.USER2_NAME);
        mContext = context;
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        final FloatingActionButton mButtonSend = rootView.findViewById(R.id.button_send);
        //Disable send button, user can't send a blank message
        mButtonSend.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.fabDisabled, null)
        ));
        mButtonSend.setEnabled(false);

        final EditText reply = rootView.findViewById(R.id.et_reply);
        //show the username that is being replied to
        reply.setHint("Send reply to " + mUser2Name);

        //only after some text is entered into the reply field will the send button become
        //available to press
        reply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (reply.getText().toString().length() > 0) {

                    mButtonSend.setBackgroundTintList(ColorStateList.valueOf(
                            getResources().getColor(R.color.colorAccent, null)
                    ));
                    mButtonSend.setEnabled(true);
                } else {
                    mButtonSend.setBackgroundTintList(ColorStateList.valueOf(
                            getResources().getColor(R.color.fabDisabled, null)
                    ));
                    mButtonSend.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle sending a message to another user
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                final DatabaseReference messageRef = reference.child("messages").child(mMessagePushKey);
                final DatabaseReference conversationRef = reference.child("conversations");
                final String user1Id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final DatabaseReference user1Ref = conversationRef.child(user1Id);
                //update the firebase node for the last message sent. ie this message
                final Long timeStamp = new Date().getTime();
                Map<String, Object> map = new HashMap<>();
                map.put("lastMessage", reply.getText().toString());
                map.put("lastMessageType", "sent");
                map.put("timeStamp", timeStamp);
                user1Ref.child(mMessagePushKey).updateChildren(map);

                final String body = reply.getText().toString();

                //add message to messages firebase node
                ChatMessage message = new ChatMessage(body,
                        mUserName, timeStamp);
                messageRef.push().setValue(message);

                //get the firebase id of the user being replied to so their last message can
                //be updated
                Query user2Query = reference.child("users")
                        .orderByChild("userName")
                        .equalTo(mUser2Name);

                user2Query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String user2Key = dataSnapshot.getKey();
                        //create notification node for firebase function to see to send a new
                        //notification to user2
                        Notification notification = new Notification(user2Key, mUserName,
                                body);
                        reference.child("Notifications").child(mMessagePushKey)
                                .push().setValue(notification);
                        DatabaseReference user2ConversationRef = reference.child("conversations")
                                .child(user2Key)
                                .child(mMessagePushKey);

                        Map<String, Object> m = new HashMap<>();
                        m.put("lastMessage", body);
                        m.put("lastMessageType", "reveived");
                        m.put("timeStamp", timeStamp);

                        user2ConversationRef.updateChildren(m);
                        AppWidgetManager manager = AppWidgetManager.getInstance(mContext);
                        int[] widgetIds = manager
                                .getAppWidgetIds(new ComponentName(getContext()
                                        .getPackageName(),
                                        AppWidget.class.getName()));
                        manager
                                .notifyAppWidgetViewDataChanged(widgetIds,
                                        R.id.lv_widget_conversations);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //clear the reply field text
                reply.getText().clear();
            }
        });


        RecyclerView recyclerView = rootView.findViewById(R.id.rv_chat);
        LinearLayoutManager ll = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(ll);
        Query reference = FirebaseDatabase.getInstance().getReference()
                .child("messages")
                .child(mMessagePushKey)
                .orderByChild("timeStamp");

        //firebase recycler adapter for displaying the chat. distinguishes between incoming and
        //outgoing messages
        mAdapter = new FirebaseRecyclerAdapter<ChatMessage, RecyclerView.ViewHolder>(
                ChatMessage.class,
                R.layout.chat_incoming,
                RecyclerView.ViewHolder.class,
                reference
        ) {
            private static final int VIEWTYPE_INCOMING = 100;
            private static final int VIEWTYPE_OUTGOING = 200;


            @Override
            protected void populateViewHolder(RecyclerView.ViewHolder viewHolder, ChatMessage model,
                                              int position) {
                ((ChatViewHolder) viewHolder).setChatBody(model.getBody());
            }

            @Override
            public int getItemViewType(int position) {
                final ChatMessage message = getItem(position);
                if (message.getSender().equals(mUserName)) {
                    return VIEWTYPE_OUTGOING;
                } else {
                    return VIEWTYPE_INCOMING;
                }
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                switch (viewType) {
                    case VIEWTYPE_OUTGOING:
                        View outgoing = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.chat_outgoing, parent, false);
                        return new ChatViewHolder(outgoing);
                    case VIEWTYPE_INCOMING:
                        View incoming = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.chat_incoming, parent, false);
                        return new ChatViewHolder(incoming);
                    default:
                        return super.onCreateViewHolder(parent, viewType);
                }
            }
        };

        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        return rootView;
    }

}
