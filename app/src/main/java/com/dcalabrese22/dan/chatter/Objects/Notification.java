package com.dcalabrese22.dan.chatter.Objects;

/**
 * Created by dan on 11/23/17.
 */

//custom object that represents a notification to be sent
public class Notification {

    private String sentTo;
    private String from;
    private String body;

    public Notification(String sentTo, String from, String body) {
        this.sentTo = sentTo;
        this.from = from;
        this.body = body;
    }

    public String getSentTo() {
        return sentTo;
    }

    public void setSentTo(String sentTo) {
        this.sentTo = sentTo;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
