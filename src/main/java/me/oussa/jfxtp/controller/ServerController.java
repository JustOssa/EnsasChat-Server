package me.oussa.jfxtp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import me.oussa.jfxtp.service.ServerService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerController {
    @FXML
    private TextArea messageText;
    @FXML
    private TextArea outputText;
    private ServerService serverService;

    public void initialize() {
        try {
            serverService = new ServerService(this);
            Registry register = LocateRegistry.createRegistry(1099);
            register.rebind("testRMI", serverService);
            System.out.println("Server is ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSendClick() throws RemoteException {
        serverService.sendToAll("Server: " + messageText.getText());
        messageText.clear();
    }

    public void printMessage(String message) {
        outputText.appendText(message + "\n");
    }
}