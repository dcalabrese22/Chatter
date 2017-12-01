package com.dcalabrese22.dan.chatter.helpers;

import android.content.res.Resources;
import android.provider.ContactsContract;

import com.dcalabrese22.dan.chatter.R;

//constants for emailloader query
public class ProfileQuery {

    public static final String[] PROJECTION = {ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY};


    public static final String SELECTION = ContactsContract.Contacts.Data.MIMETYPE +
            " = ?";

    public static final String[] ARGS = new String[]{ContactsContract.CommonDataKinds.Email
            .CONTENT_ITEM_TYPE};

    static String desc = Resources.getSystem().getString(R.string.sort_desc);
    public static final String SORT = ContactsContract.Contacts.Data.IS_PRIMARY + " " + desc;


}
