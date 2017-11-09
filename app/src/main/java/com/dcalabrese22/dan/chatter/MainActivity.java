package com.dcalabrese22.dan.chatter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dcalabrese22.dan.chatter.Objects.User;
import com.dcalabrese22.dan.chatter.fragments.ChatFragment;
import com.dcalabrese22.dan.chatter.fragments.MessagesListFragment;
import com.dcalabrese22.dan.chatter.interfaces.MessageExtrasListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity implements MessageExtrasListener {

    private String mCorrespondent;
    public static final String MESSAGE_PUSH_KEY = "message_push_key";
    public static final String USER_NAME = "user_name";
    public static final String USER2_NAME = "user2_name";
    private String mUserName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MessagesListFragment fragment = new MessagesListFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, fragment)
                .commit();

        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference();

        tokenRef.child("RefreshTokens").child(userId).child(refreshToken).setValue(true);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId);
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


    }

    @Override
    public void getMessageExtras(String id, String user2) {
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE_PUSH_KEY, id);
        bundle.putString(USER_NAME, mUserName);
        bundle.putString(USER2_NAME, user2);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
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
