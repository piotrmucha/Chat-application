package message;

import java.io.Serializable;

public class Message implements Serializable {
    private String userName;
    private String messageContent;
    private KindOfMessage kindOfMessage;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public KindOfMessage getKindOfMessage() {
        return kindOfMessage;
    }

    public void setKindOfMessage(KindOfMessage kindOfMessage) {
        this.kindOfMessage = kindOfMessage;
    }
}
