package com.dcalabrese22.dan.chatter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dcalabrese22.dan.chatter.helpers.EmailLoader;
import com.dcalabrese22.dan.chatter.helpers.LogoAsyncTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//activity for logging a user in to the chat app
public class LoginActivity extends AppCompatActivity {


    private static final String TAG = "LoginActivity";
    private static final String SAVED_EMAIL = "saved_email";
    private static final String SAVE_PASSWORD = "saved_password";
    private static final int REQUEST_READ_CONTACTS = 100;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private AutoCompleteTextView mEmail;
    private EditText mPassword;
    private Button mSignInButton;
    private Context mContext;
    private ProgressBar mProgressbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button joinButton = (Button) findViewById(R.id.new_user_button);
        mEmail = (AutoCompleteTextView) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mContext = this;
        mProgressbar = (ProgressBar) findViewById(R.id.login_progress);
        ImageView logo = findViewById(R.id.imageview_logo);

        new LogoAsyncTask().execute(logo);

        if (savedInstanceState != null) {
            mEmail.setText(savedInstanceState.getString(SAVED_EMAIL));
            mPassword.setText(savedInstanceState.getString(SAVE_PASSWORD));
        }

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        //signs the user in if account is valid
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser();
            }
        });

        //starts the register new user activity
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, RegisterUserActivity.class);
                startActivity(intent);

            }
        });


        mPassword.setOnEditorActionListener(new EditText.OnEditorActionListener(){
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signInUser();
                    return true;
                }
                return false;
            }
        });

        populateAutoComplete();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //signs in the user to the app
    public void signInUser() {

        //checks if the user has entered data into the fields
        if (mEmail.getText().toString().equals("") || mPassword.getText().toString().equals("")) {
            Toast.makeText(mContext, R.string.enter_credentials, Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //if the user signs in, show the progress bar and start the mainactivity
                            if (task.isSuccessful()) {
                                mProgressbar.setVisibility(View.VISIBLE);
                                Intent intent = new Intent(mContext, MainActivity.class);
                                startActivity(intent);
                            }
                            //alert the user the credentials they entered are incorrect
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
        }
    }

    //shows a pop up asking if the app can read contacts from the devices to use in autocompleting
    //email address
    private boolean mayRequestContacts() {

        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            Snackbar.make(mEmail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        EmailLoader emailLoader = new EmailLoader(this, mEmail);
        getLoaderManager().initLoader(0, null, emailLoader);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String enteredEmail = mEmail.getText().toString();
        String enteredPass = mPassword.getText().toString();
        outState.putString(SAVED_EMAIL, enteredEmail);
        outState.putString(SAVE_PASSWORD, enteredPass);
    }
}

