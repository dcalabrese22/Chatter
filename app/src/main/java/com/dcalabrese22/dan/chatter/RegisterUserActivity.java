package com.dcalabrese22.dan.chatter;

import android.*;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.dcalabrese22.dan.chatter.Objects.User;
import com.dcalabrese22.dan.chatter.helpers.EmailLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dcalabrese on 11/3/2017.
 */

public class RegisterUserActivity extends AppCompatActivity {

    @BindView(R.id.signup_input_name)
    EditText mName;
    @BindView(R.id.signup_input_email)
    EditText mEmail;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_register_user);

//        mName = findViewById(R.id.signup_input_name);
//        mEmail = findViewById(R.id.signup_input_email);
//        mPassword = findViewById(R.id.signup_input_password);
//        mAge = findViewById(R.id.signup_input_age);
//        mRadioGroup = findViewById(R.id.gender_radio_group);
//        mSignup = find

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        mSignup.setOnClickListener(new SignupOnClickListener());
        mBrowse.setOnClickListener(new BrowseOnClickListener());

    }

    private class SignupOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String name = mName.getText().toString();
            String email = mEmail.getText().toString();
            String age = mAge.getText().toString();
            String password = mPassword.getText().toString();
            int radioId = mRadioGroup.getCheckedRadioButtonId();
            String gender;
            switch (radioId) {
                case (R.id.male_radio_btn):
                    gender = "male";
                    break;
                case (R.id.female_radio_btn):
                    gender = "male";
                    break;
                default:
                    gender = null;
                    break;
            }


            final User user = new User(name, email, age, gender, mHasUserImage);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            FirebaseDatabase.getInstance().getReference()
                                    .child("users")
                                    .child(firebaseUser.getUid())
                                    .push()
                                    .setValue(user);
                            uploadAvatarToFirebase(mBitmapByteArray);
                            Intent intent = new Intent(mContext, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        }
    }

    public void uploadAvatarToFirebase(byte[] dataArray) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference imagesRef = storageReference.child("images");
        StorageReference userImageRef = imagesRef
                .child(mAuth.getCurrentUser().getEmail())
                .child("avatar.jpg");
        UploadTask uploadTask = userImageRef.putBytes(dataArray);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Success", " yes");
            }
        });
    }

    private class BrowseOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE)
            if (resultCode == RESULT_OK) {
                if (data == null) {
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
                        mHasUserImage = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }

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

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        EmailLoader emailLoader = new EmailLoader(this, mEmail);
        getLoaderManager().initLoader(0, null, emailLoader);
    }
}
