package com.dcalabrese22.dan.chatter.interfaces;

/**
 * Created by dan on 9/14/17.
 */

public interface MessageExtrasListener {

    void getMessageExtras(String id, String user2);
    void getMessageExtras(String id, String user2, boolean cameFromWidget);
}
