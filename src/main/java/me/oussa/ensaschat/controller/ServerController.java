package me.oussa.ensaschat.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import me.oussa.ensaschat.service.ServerService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerController {

    @FXML
    private TextField portField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private TextField ipField;
    @FXML
    private Label statusLabel;
    @FXML
    private Label clientsCountLabel;
    @FXML
    private TextArea messageText;
    @FXML
    private TextArea outputText;
    private ServerService serverService;


    // Get server ip address
    private String getIpAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "Localhost";
        }
    }

    @FXML
    protected void onStartServer() {

        int port;
        try {
            port = Integer.parseInt(portField.getText());
            if (port < 0 || port > 65535) {
                throw new Exception();
            }
        } catch (Exception e) {
            errorLabel.setText("Invalid port");
            portField.setText("1099");
            return;
        }

        // Create a new registry or get the existing one if it's already created
        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            try {
                registry = LocateRegistry.getRegistry(port);
            } catch (RemoteException ex) {
                ex.printStackTrace();
                errorLabel.setText("Error starting server");
                return;
            }
        }

        try {
            serverService = new ServerService(this);
            registry.rebind("testRMI", serverService);
            System.out.println("Server started on port " + port);
            startButton.setDisable(true);
            stopButton.setDisable(false);
            portField.setDisable(true);
            ipField.setText(getIpAddress());
            errorLabel.setText("");
            statusLabel.setTextFill(Color.valueOf("#00861b"));
            statusLabel.setText("Running");
        } catch (RemoteException e) {
            errorLabel.setText("Error starting server");
            e.printStackTrace();
        }

    }

    @FXML
    protected void onStopServer() {
        // TODO: Loop through all clients and disconnect them
        // kick all clients
        serverService.kickAll();

        try {
            LocateRegistry.getRegistry().unbind("testRMI");
            UnicastRemoteObject.unexportObject(serverService, true);
            System.out.println("Server stopped");
            startButton.setDisable(false);
            stopButton.setDisable(true);
            portField.setDisable(false);
            ipField.setText("");
            statusLabel.setTextFill(Color.valueOf("#c40000"));
            statusLabel.setText("Stopped");
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

    // update the clients count label
    public void updateClientsCount(int count) {
        clientsCountLabel.setText(String.valueOf(count));
    }
}