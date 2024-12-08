package com.papa.app.dto;

public class ChatMessage extends BaseMessage {
    private String message;

    public ChatMessage() {
        setType("chat");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}