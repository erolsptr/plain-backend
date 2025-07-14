package com.planit.model;

// Lombok'u pom.xml'e ekleyerek bu class'ı daha temiz hale getireceğiz.
// Şimdilik manuel olarak getter/setter ekleyelim.
public class Message {
    private String sender;
    private String content;
    private MessageType type;

    public enum MessageType {
        JOIN,
        VOTE,
        LEAVE
    }

    // Getter'lar ve Setter'lar
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    // Message.java'ya eklenecek
public Message() {}
}
