package com.dcalabrese22.dan.chatter.Objects;

/**
 * Created by dcalabrese on 11/13/2017.
 */

//custom object that represents a message displayed on the widget
public class WidgetListItem {

    private String sender;
    private String lastMessage;
    private String conversationId;
    private String avatar;

    public WidgetListItem() {}

    public WidgetListItem(String sender, String lastMessage, String conversationId) {
        this.sender = sender;
        this.lastMessage = lastMessage;
        this.conversationId = conversationId;
    }

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
