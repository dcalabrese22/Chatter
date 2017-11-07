package com.dcalabrese22.dan.chatter.helpers;

import android.provider.ContactsContract;


public class ProfileQuery {

    public static final String[] PROJECTION = {ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY};


    public static final String SELECTION = ContactsContract.Contacts.Data.MIMETYPE +
            " = ?";

    public static final String[] ARGS = new String[]{ContactsContract.CommonDataKinds.Email
            .CONTENT_ITEM_TYPE};

    public static final String SORT = ContactsContract.Contacts.Data.IS_PRIMARY + " DESC";


}
