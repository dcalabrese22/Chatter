package com.dcalabrese22.dan.chatter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dcalabrese22.dan.chatter.Objects.Conversation;
import com.dcalabrese22.dan.chatter.Objects.User;
import com.dcalabrese22.dan.chatter.fragments.ChatFragment;
import com.dcalabrese22.dan.chatter.fragments.MessagesListFragment;
import com.dcalabrese22.dan.chatter.fragments.NewMessageFragment;
import com.dcalabrese22.dan.chatter.interfaces.MessageExtrasListener;
import com.dcalabrese22.dan.chatter.services.ChatterFirebaseMessagingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity implements MessageExtrasListener {

    private String mCorrespondent;
    public static final String MESSAGE_PUSH_KEY = "message_push_key";
    public static final String USER_NAME = "user_name";
    public static final String USER2_NAME = "user2_name";
    private String mUserName;
    private String mUserId;
    private boolean mCameFromWidgetOrNotification = false;
    public static final String CAME_FROM_WIDGE_TO_NEW_MESSAGE = "came_from_widget_to_new_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(mUserId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUserName = user.getUserName();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Intent launchedIntent = getIntent();
        Log.d("launched intent extra?",
                String.valueOf(launchedIntent.hasExtra(AppWidget.WIDGET_INTENT_EXTRA)));

        if (launchedIntent.hasExtra(AppWidget.WIDGET_INTENT_EXTRA)) {
            String launchedIntentValue = launchedIntent.getStringExtra(AppWidget.WIDGET_INTENT_EXTRA);
            Log.d("launchedIntentValue:", launchedIntentValue);
            if (launchedIntentValue.equals(AppWidget.NEW_MESSAGE_FRAGMENT_VALUE)) {
                NewMessageFragment newMessageFragment = new NewMessageFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean(CAME_FROM_WIDGE_TO_NEW_MESSAGE, true);
                newMessageFragment.setArguments(bundle);
                transaction.add(R.id.fragment_container, newMessageFragment).commit();
            }
            if (launchedIntentValue.equals(WidgetDataProvider.CONVERSATION_FRAGMENT_VALUE)) {
                String messageId = launchedIntent.getStringExtra(WidgetDataProvider.WIDGET_CONVERSATION_ID_EXTRA);
                getCorrespondentAndStartChat(messageId);
                mCameFromWidgetOrNotification = true;
            }
        } else if (launchedIntent.hasExtra(ChatterFirebaseMessagingService.FROM_NOTIFICATION)){
            String messageId = launchedIntent
                    .getStringExtra(ChatterFirebaseMessagingService.FROM_NOTIFICATION);
            getCorrespondentAndStartChat(messageId);
            mCameFromWidgetOrNotification = true;
        } else {

            MessagesListFragment fragment = new MessagesListFragment();
            transaction.add(R.id.fragment_container, fragment)
                    .commit();

            String refreshToken = FirebaseInstanceId.getInstance().getToken();
            DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference();

            tokenRef.child("RefreshTokens").child(mUserId).child(refreshToken).setValue(true);

        }
    }

    public void getCorrespondentAndStartChat(final String messageId) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("conversations")
                .child(mUserId)
                .child(messageId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Conversation conversation = dataSnapshot.getValue(Conversation.class);
                String user2 = conversation.getUser2();
                Bundle bundle = new Bundle();
                bundle.putString(MESSAGE_PUSH_KEY, messageId);
                bundle.putString(USER_NAME, mUserName);
                bundle.putString(USER2_NAME, user2);
                launchChatFragment(bundle);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void launchChatFragment(Bundle bundle) {
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void getMessageExtras(String id, String user2) {
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE_PUSH_KEY, id);
        bundle.putString(USER_NAME, mUserName);
        bundle.putString(USER2_NAME, user2);
        launchChatFragment(bundle);
    }

    @Override
    public void getMessageExtras(String id, String user2, boolean cameFromWidget) {
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE_PUSH_KEY, id);
        bundle.putString(USER_NAME, mUserName);
        bundle.putString(USER2_NAME, user2);
        mCameFromWidgetOrNotification = cameFromWidget;
        launchChatFragment(bundle);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0 || mCameFromWidgetOrNotification) {
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case  R.id.logout:
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
