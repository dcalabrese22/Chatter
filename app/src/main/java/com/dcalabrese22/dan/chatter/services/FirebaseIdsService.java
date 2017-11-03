package com.dcalabrese22.dan.chatter.services;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by dcalabrese on 10/16/2017.
 */

public class FirebaseIdsService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
//        String refreshToken = FirebaseInstanceId.getInstance().getToken();
//        SharedPreferences preferences = getSharedPreferences(getResources()
//                .getString(R.string.firebase_token_pref),Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString(getResources().getString(R.string.current_firebase_token), refreshToken);
//        editor.commit();
    }

}

