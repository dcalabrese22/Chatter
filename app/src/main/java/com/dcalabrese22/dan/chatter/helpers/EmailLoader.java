package com.dcalabrese22.dan.chatter.helpers;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dcalabrese on 11/6/2017.
 */

//loads email addresses from users contact list to aid in signing in or registering
public class EmailLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;
    private AutoCompleteTextView mEmail;

    public EmailLoader(Context context, AutoCompleteTextView textView) {
        mContext = context;
        mEmail = textView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext,
                ContactsContract.Data.CONTENT_URI,
                ProfileQuery.PROJECTION,
                ProfileQuery.SELECTION,
                ProfileQuery.ARGS,
                ProfileQuery.SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<String> emails = new ArrayList<>();
        data.moveToFirst();
        while (!data.isAfterLast()) {
            emails.add(data.getString(0));
            data.moveToNext();
            addEmailsToAutoComplete(emails);
        }
    }

    private void addEmailsToAutoComplete(List<String> emails) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_dropdown_item_1line, emails);
        mEmail.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
