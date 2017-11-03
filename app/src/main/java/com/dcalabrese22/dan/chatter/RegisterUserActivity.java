package com.dcalabrese22.dan.chatter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register_user);

//        mName = findViewById(R.id.signup_input_name);
//        mEmail = findViewById(R.id.signup_input_email);
//        mPassword = findViewById(R.id.signup_input_password);
//        mAge = findViewById(R.id.signup_input_age);
//        mRadioGroup = findViewById(R.id.gender_radio_group);
//        mSignup = find

        ButterKnife.bind(this);

        mSignup.setOnClickListener(new SignupOnClickListener());
        mBrowse.setOnClickListener(new BrowseOnClickListener());

    }

    private class SignupOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String name = mName.getText().toString();
            String email = mEmail.getText().toString();
            String age = mAge.getText().toString();
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


        }
    }

    private class BrowseOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("requestCode: ", String.valueOf(requestCode));
        Log.d("resultCode: ", String.valueOf(resultCode));
        if (requestCode == PICK_IMAGE)
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    return;
                } else {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReference();
                        StorageReference imagesRef = storageReference.child("images");
                        StorageReference userImageRef = imagesRef
                                .child(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                                .child("avatar.jpg");
                        UploadTask uploadTask = userImageRef.putStream(inputStream);
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

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    }
}
