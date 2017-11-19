package com.dcalabrese22.dan.chatter.fragments;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.dcalabrese22.dan.chatter.MainActivity;
import com.dcalabrese22.dan.chatter.Objects.ChatMessage;
import com.dcalabrese22.dan.chatter.Objects.Conversation;
import com.dcalabrese22.dan.chatter.Objects.User;
import com.dcalabrese22.dan.chatter.AppWidget;
import com.dcalabrese22.dan.chatter.R;
import com.dcalabrese22.dan.chatter.interfaces.MessageExtrasListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class NewMessageFragment extends Fragment {

    private AutoCompleteTextView mName;
    private EditText mBody;
    private boolean mCameFromWidget = false;
    private String mPushKey;
    private String mUser1Name;
    private String mUser2Name;
    private MessageExtrasListener mListener;
    private Context mContext;


    public NewMessageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MessageExtrasListener) context;
        mContext = context;
        if (getArguments() != null) {
            if (getArguments().containsKey(MainActivity.CAME_FROM_WIDGE_TO_NEW_MESSAGE)) {
                mCameFromWidget = getArguments()
                        .getBoolean(MainActivity.CAME_FROM_WIDGE_TO_NEW_MESSAGE);
                Log.d("mCameFromWidget", String.valueOf(mCameFromWidget));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_message, container, false);
        mName = rootView.findViewById(R.id.et_new_message_to);
        mBody = rootView.findViewById(R.id.et_new_message_body);
        FloatingActionButton fab = rootView.findViewById(R.id.fab_new_message);

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = reference.child("users");

        final List<String> autoCompleteNames = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = user.getUid();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (!child.getKey().equals(userId)) {
                        User other = child.getValue(User.class);
                        String userName = other.getUserName();
                        autoCompleteNames.add(userName);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, autoCompleteNames);

        mName.setAdapter(adapter);
        fab.setOnClickListener(new SendNewMessageListener());

        return rootView;
    }

    private class SendNewMessageListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (mName.getText().toString().equals("") || mBody.getText().toString().equals("")) {
                Toast.makeText(getContext(), R.string.missing_fields,
                        Toast.LENGTH_SHORT).show();
            } else {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                final Long timeStamp = new Date().getTime();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final DatabaseReference conversationRef = reference.child("conversations")
                        .child(currentUserId)
                        .push();
                mPushKey = conversationRef.getKey();
                final DatabaseReference messagesRef = reference.child("messages")
                        .child(mPushKey).push();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(currentUserId);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        final String userImageRef = currentUser.getImageUrl();
                        mUser1Name = currentUser.getUserName();
                        mUser2Name = mName.getText().toString();
                        reference.child("users")
                                .orderByChild("userName").equalTo(mUser2Name)
                                .addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        String user2Key = dataSnapshot.getKey();
                                        User user2 = dataSnapshot.getValue(User.class);
                                        String user2ImageRef = user2.getImageUrl();
                                        Log.d("user2Key", user2Key);
                                        Conversation conversationFromUser1 = new Conversation(mBody.getText().toString(),
                                                "sent", timeStamp, mUser1Name, mUser2Name,
                                                mPushKey, userImageRef, user2ImageRef);
                                        conversationRef.setValue(conversationFromUser1);
                                        Conversation receivedByUser2 = new Conversation(mBody.getText().toString(),
                                                "received", timeStamp, mUser2Name,
                                                mUser1Name, mPushKey, user2ImageRef, userImageRef);
                                        DatabaseReference user2ConversationRef = reference.child("conversations")
                                                .child(user2Key).child(mPushKey);
                                        user2ConversationRef.setValue(receivedByUser2);
                                        ChatMessage message = new ChatMessage(mBody.getText().toString(),
                                                mUser1Name, timeStamp);
                                        messagesRef.setValue(message);
                                        mListener.getMessageExtras(mPushKey, mUser2Name, mCameFromWidget);
                                        Log.d("new msg frag", mUser2Name);
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

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            AppWidgetManager manager = AppWidgetManager.getInstance(mContext);
            int[] widgetIds = manager
                    .getAppWidgetIds(new ComponentName(getContext()
                            .getPackageName(),
                            AppWidget.class.getName()));
            manager
                    .notifyAppWidgetViewDataChanged(widgetIds,
                            R.id.lv_widget_conversations);
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

}
