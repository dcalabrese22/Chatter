package com.dcalabrese22.dan.chatter.Objects;

/**
 * Created by dan on 9/7/17.
 */

public class ChatMessage {

    private String body;
    private String sender;
    private String type;
    private Long timeStamp;

    public ChatMessage() {}

    public ChatMessage(String body, String sender, String type, Long timeStamp) {
        this.body = body;
        this.sender = sender;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return body;
    }
}

