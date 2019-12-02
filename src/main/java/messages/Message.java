package messages;

import java.io.Serializable;

public class Message implements Serializable {
    private String userName;
      private String content;
      private KindOfMessage kindOfMessage;
      private int usersCounter;

    public int getUsersCounter() {
        return usersCounter;
    }

    public void setUsersCounter(int usersCounter) {
        this.usersCounter = usersCounter;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public KindOfMessage getKindOfMessage() {
        return kindOfMessage;
    }

    public void setKindOfMessage(KindOfMessage kindOfMessage) {
        this.kindOfMessage = kindOfMessage;
    }
}
