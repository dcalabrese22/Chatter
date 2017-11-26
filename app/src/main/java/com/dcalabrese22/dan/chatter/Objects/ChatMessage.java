package com.dcalabrese22.dan.chatter.Objects;

/**
 * Created by dan on 9/7/17.
 */

//custom ovject that represents a single chat message
public class ChatMessage {

    private String body;
    private String sender;
    private Long timeStamp;

    public ChatMessage() {}

    public ChatMessage(String body, String sender, Long timeStamp) {
        this.body = body;
        this.sender = sender;
        this.timeStamp = timeStamp;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getBody() {
        return body;
    }

    public String getSender() {
        return sender;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return body;
    }
}

