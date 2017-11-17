package com.dcalabrese22.dan.chatter.Objects;

/**
 * Created by dan on 9/7/17.
 */

public class Conversation {

    private String lastMessage;
    private String lastMessageType;
    private Long timeStamp;
    private String user1;
    private String user2;
    private String conversationId;
    private String user1ImageRef;
    private String user2ImageRef;

    public Conversation() {}

    public Conversation(String lastMessage, String lastMessageType, Long timeStamp, String user1,
                        String user2, String conversationId) {
        this.lastMessage = lastMessage;
        this.lastMessageType = lastMessageType;
        this.timeStamp = timeStamp;
        this.user1 = user1;
        this.user2 = user2;
        this.conversationId = conversationId;
    }

    public Conversation(String lastMessage, String lastMessageType, Long timeStamp, String user1,
                        String user2, String conversationId, String user1ImageRef,
                        String user2ImageRef) {
        this.lastMessage = lastMessage;
        this.lastMessageType = lastMessageType;
        this.timeStamp = timeStamp;
        this.user1 = user1;
        this.user2 = user2;
        this.conversationId = conversationId;
        this.user1ImageRef = user1ImageRef;
        this.user2ImageRef = user2ImageRef;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public String getLastMessageType() {
        return lastMessageType;
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

    public String getUser1ImageRef() {
        return user1ImageRef;
    }

    public void setUser1ImageRef(String user1ImageRef) {
        this.user1ImageRef = user1ImageRef;
    }

    public String getUser2ImageRef() {
        return user2ImageRef;
    }

    public void setUser2ImageRef(String user2ImageRef) {
        this.user2ImageRef = user2ImageRef;
    }

    @Override
    public boolean equals(Object obj) {
        Conversation c = (Conversation) obj;
        if (c.getConversationId().equals(conversationId)) {
            return true;
        }
        return false;
    }
}

