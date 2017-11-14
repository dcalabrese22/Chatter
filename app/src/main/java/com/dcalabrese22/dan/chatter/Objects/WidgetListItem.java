package com.dcalabrese22.dan.chatter.Objects;

/**
 * Created by dcalabrese on 11/13/2017.
 */

public class WidgetListItem {

    private String sender;
    private String lastMessage;
    private String conversationId;

    public WidgetListItem() {}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
