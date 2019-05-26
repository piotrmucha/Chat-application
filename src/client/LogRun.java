package client;

import javafx.scene.Scene;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class LogRun {
    Scene scene;
    Socket s;
    LogController myControllerHandle;
    final static int ServerPort = 4999;
    public LogRun(Scene scene, LogController controller  ) {
        this.scene = scene;
        this.myControllerHandle = controller;
    }

}
