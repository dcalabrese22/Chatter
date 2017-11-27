package com.dcalabrese22.dan.chatter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.dcalabrese22.dan.chatter.Objects.User;
import com.dcalabrese22.dan.chatter.helpers.EmailLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;


//Activity for creating a new user
public class RegisterUserActivity extends AppCompatActivity {

    @BindView(R.id.signup_input_name)
    EditText mName;
    @BindView(R.id.signup_input_email)
    AutoCompleteTextView mEmail;
    @BindView(R.id.signup_input_password)
    EditText mPassword;
    @BindView(R.id.signup_input_age)
    EditText mAge;
    @BindView(R.id.gender_radio_group)
    RadioGroup mRadioGroup;

    @BindView(R.id.btn_signup)
    Button mSignup;
    @BindView(R.id.btn_link_login)
    Button mLogin;
    @BindView(R.id.btn_browse)
    Button mBrowse;

    public static final int PICK_IMAGE = 1;
    private FirebaseAuth mAuth;
    private boolean mHasUserImage = false;
    private Context mContext;
    private byte[] mBitmapByteArray;
    private static final int REQUEST_READ_CONTACTS = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_register_user);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        populateAutoComplete();

        mSignup.setOnClickListener(new SignupOnClickListener());
        mBrowse.setOnClickListener(new BrowseOnClickListener());
        mLogin.setOnClickListener(new LoginOnClickListener());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //Custom click listener for opening the login activity
    private class LoginOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //Custom click listener for signing a user up
    private class SignupOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final String userName = mName.getText().toString();
            final String email = mEmail.getText().toString();
            final String age = mAge.getText().toString();
            final String password = mPassword.getText().toString();
            final int radioId = mRadioGroup.getCheckedRadioButtonId();
            final StringBuilder gender = new StringBuilder();
            switch (radioId) {
                case (R.id.male_radio_btn):
                    gender.append("male");
                    break;
                case (R.id.female_radio_btn):
                    gender.append("female");
                    break;
                default:
                    break;
            }

            //first check firebase for a user with that username
            DatabaseReference existingUser = FirebaseDatabase.getInstance()
                    .getReference().child("users").child(userName);
            existingUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //alert the user to choose a new username if one already exists
                    if (dataSnapshot.exists()) {
                        Toast.makeText(mContext, "Username already exists. Please sign in to " +
                                "continue or choose a new username.", Toast.LENGTH_LONG).show();
                        //firebase won't accept passwords less than 6 characters
                    } else if (password.length() < 6) {
                        Toast.makeText(mContext,
                                "Password must be at least 6 characters",
                                Toast.LENGTH_LONG).show();
                        //if all is ok, create the new user and start the main activity
                    } else {
                        final User user = new User(userName, email, age, gender.toString(), mHasUserImage);
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        String url = uploadAvatarToFirebase(mBitmapByteArray);
                                        Log.d("url ref", url);
                                        user.setImageUrl(url);
                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("users")
                                                .child(firebaseUser.getUid())
                                                .setValue(user);
                                        Intent intent = new Intent(mContext, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Uploads a user image to Firebase storage
     * @param dataArray The image as an array of bytes
     * @return String path to Firebase storage location
     */
    public String uploadAvatarToFirebase(byte[] dataArray) {
        final StringBuilder imageUrlBuilder = new StringBuilder();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference imagesRef = storageReference.child("images");
        imageUrlBuilder.append(imagesRef.getBucket());
        final StorageReference userImageRef = imagesRef
                .child(mAuth.getCurrentUser().getUid())
                .child("avatar.jpg");
        if (dataArray != null) {
            UploadTask uploadTask = userImageRef.putBytes(dataArray);
            imageUrlBuilder.append(userImageRef.getPath());
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });
        } else {
            String defaultLocation = "/images/default/avatar.jpg";
            imageUrlBuilder.append(defaultLocation);

        }
        return imageUrlBuilder.toString();
    }

    //Custom click listener for starting an intent to browse for an image stored on the device
    private class BrowseOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE);
        }
    }

    //Gets the result of the image chosen by the user
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE)
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    mHasUserImage = true;
                    return;
                } else {
                    try {
                        Uri imageUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media
                                .getBitmap(this.getContentResolver(), imageUri);
                        bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        mBitmapByteArray = baos.toByteArray();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }

    /**
     * Determines if the user has granted permission to read from the contacts
     * @return
     */
    private boolean mayRequestContacts() {

        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS)) {
            Snackbar.make(mEmail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Suggests autocomplet values for the email field based on contacts stored on the device
     */
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        EmailLoader emailLoader = new EmailLoader(this, mEmail);
        getLoaderManager().initLoader(0, null, emailLoader);
    }
}
