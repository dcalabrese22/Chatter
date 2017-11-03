package com.dcalabrese22.dan.chatter.Objects;

/**
 * Created by dan on 9/7/17.
 */

public class Conversation {

    private String id;
    private String title;
    private String lastMessage;
    private String lastMessageType;
    private Long timeStamp;
    private String pushKey;

    public Conversation() {}

    public Conversation(String id, String title, String user, String lastMessage, String userImage,
                        String lastMessageType, Long timeStamp, String pushKey) {
        this.title = title;
        this.id = id;
        this.lastMessage = lastMessage;

        this.lastMessageType = lastMessageType;
        this.timeStamp = timeStamp;
        this.pushKey = pushKey;
    }

    public String getPushKey() {
        return pushKey;
    }

    public void setPushKey(String pushKey) {
        this.pushKey = pushKey;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public String getLastMessageType() {
        return lastMessageType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageType(String lastMessageType) {
        this.lastMessageType = lastMessageType;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object obj) {
        Conversation c = (Conversation) obj;
        if (c.getId().equals(id)) {
            return true;
        } else {
            return false;
        }
    }
}

