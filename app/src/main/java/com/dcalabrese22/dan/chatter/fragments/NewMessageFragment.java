package com.dcalabrese22.dan.chatter.fragments;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.dcalabrese22.dan.chatter.Objects.ChatMessage;
import com.dcalabrese22.dan.chatter.Objects.Conversation;
import com.dcalabrese22.dan.chatter.Objects.User;
import com.dcalabrese22.dan.chatter.PbAppWidget;
import com.dcalabrese22.dan.chatter.R;
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

    AutoCompleteTextView mName;
    EditText mBody;


    public NewMessageFragment() {
        // Required empty public constructor
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

        fab.setOnClickListener(new SendNewMessageListener());

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
                final String pushKey = conversationRef.getKey();
                final DatabaseReference messagesRef = reference.child("messages")
                        .child(pushKey).push();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(currentUserId);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        final String user1 = currentUser.getUserName();
                        final String user2 = mName.getText().toString();
                        reference.child("users")
                                .orderByChild("userName").equalTo(user2)
                                .addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        String user2Key = dataSnapshot.getKey();
                                        Log.d("user2Key", user2Key);
                                        Conversation conversationFromUser1 = new Conversation(mBody.getText().toString(),
                                                "sent", timeStamp, user1, user2, pushKey);
                                        conversationRef.setValue(conversationFromUser1);
                                        Conversation receivedByUser2 = new Conversation(mBody.getText().toString(),
                                                "received", timeStamp, user2, user1, pushKey);
                                        DatabaseReference user2ConversationRef = reference.child("conversations")
                                                .child(user2Key).child(pushKey);
                                        user2ConversationRef.setValue(receivedByUser2);
                                        ChatMessage message = new ChatMessage(mBody.getText().toString(),
                                                user1, timeStamp);
                                        messagesRef.setValue(message);
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
            getActivity().getSupportFragmentManager().popBackStack();
            AppWidgetManager manager = AppWidgetManager.getInstance(getContext());
            int[] widgetIds = manager.getAppWidgetIds(new ComponentName(getContext().getPackageName(), PbAppWidget.class.getName()));
            manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.lv_widget_conversations);
        }
    }

}
