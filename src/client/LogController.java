package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;



public class LogController  {
    @FXML private TextField username;
    final static int ServerPort = 4999;
    public LogController(){}
    public void initialize () {

        Task task = new Task<Void>() {
            @Override
            public Void call() {
                try {
                    System.out.println(Thread.currentThread().getName());
                    //  Thread.sleep(1000);
                    InetAddress ip = InetAddress.getByName("10.220.3.36");

                    // establish the connection

                    Socket s = new Socket(ip, ServerPort);

                    // obtaining input and out streams

                    ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
                    ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
                    int b = 0;
                    //    } catch (InterruptedException e) {
                    //       Thread.currentThread().interrupt();
                    // code for stopping current task so thread stops

                } catch (IOException e) {
                    int k = 0;
                }
                return null;
            }
        };
        new Thread(task).start();





    }
    @FXML
    void loginClick() {
        String user = username.getText();
        int k = 0;
    }
}
