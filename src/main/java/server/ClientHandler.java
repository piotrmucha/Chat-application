package server;

import messages.KindOfMessage;
import messages.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ClientHandler implements Runnable {

    private static Logger logger = LogManager.getLogger(ClientHandler.class);
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private String name;
    private Server server;
    private Socket socket;
    private Boolean logged = true;

    ClientHandler(Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.server = Server.getInstance();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        while (true) {
            Message received = getMessage();
            switch (received.getKindOfMessage()) {
                case TRY_TO_CONNECT: {
                    handleTryToConnectMessage(received);
                    break;
                }
                case CONNECTION: {
                    this.setName(received.getUserName());
                    break;
                }
                case STANDARD_MESSAGE: {
                    notifyUsers(received);
                    break;
                }
                case DISCONNECTION: {
                    handleDisconnectionMessage(received);
                    logged = false;
                    break;
                }
                case SOFT_DISCONNETION: {
                    server.getClientHandlers().remove(this);
                    logged = false;
                    break;
                }
            }
            if (!logged) {
                break;
            }
        }
        closeStreams();
    }

    private void notifyAllPossibleClients(Message message) {
        try {
            for (ClientHandler mc : server.getClientHandlers()) {
                if (mc != this && mc.getName() != null) {
                    mc.outputStream.writeObject(message);
                }
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    private void handleDisconnectionMessage(Message received) {
        server.decrementLoggedClients();
        received.setUsersCounter(server.getLoggedClients());
        notifyAllPossibleClients(received);
        server.getClientHandlers().remove(this);
    }

    private void notifyUsers(Message received) {
        try {
            for (ClientHandler mc : server.getClientHandlers()) {
                if (mc != this) {
                    mc.outputStream.writeObject(received);
                }
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    private void closeStreams() {
        try {
            this.socket.close();
            this.inputStream.close();
            this.outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTryToConnectMessage(Message received) {
        for (ClientHandler client : server.getClientHandlers()) {
            if (nickAlreadyAssignedToAnotherUser(received, client)) {
                sentMessageToServer(received, KindOfMessage.DISCONNECTION);
                return;
            }
        }
        if (server.getLoggedClients() < 10) {
            notifyYourselfAboutConnection(received);
            received.setKindOfMessage(KindOfMessage.USER_COUNTER);
            notifyUsers(received);
        } else if (server.getLoggedClients() >= 10) {
            sentMessageToServer(received, KindOfMessage.USERS_LIMIT);
        }
    }

    private void notifyYourselfAboutConnection(Message received) {
        try {
            received.setKindOfMessage(KindOfMessage.CONNECTION);
            server.incrementLoggedClients();
            received.setUsersCounter(server.getLoggedClients());
            this.setName(received.getUserName());
            outputStream.writeObject(received);
        } catch (IOException exc) {
            logger.error("Error while notify yourself about successful connection");
            exc.printStackTrace();
        }
    }

    private void sentMessageToServer(Message received, KindOfMessage disconnection) {
        try {
            received.setKindOfMessage(disconnection);
            this.outputStream.writeObject(received);
        } catch (IOException exc) {
            logger.error("error while try to send message to server");
            exc.printStackTrace();
        }
    }

    private boolean nickAlreadyAssignedToAnotherUser(Message received, ClientHandler client) {
        return received.getUserName().equals(client.name);
    }

    private Message getMessage() {
        Message message = new Message();
        try {
            message = (Message) inputStream.readObject();
        } catch (ClassNotFoundException | IOException exc) {
            logger.error("Error while parsing object from inputStream to Message object, error: {}", exc.getLocalizedMessage());
            exc.printStackTrace();
        }
        return message;
    }
}
