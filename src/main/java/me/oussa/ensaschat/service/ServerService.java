package me.oussa.ensaschat.service;

import javafx.application.Platform;
import me.oussa.ensaschat.common.ClientInterface;
import me.oussa.ensaschat.common.ServerInterface;
import me.oussa.ensaschat.controller.ServerController;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerService extends UnicastRemoteObject implements ServerInterface {

    private final ServerController serverController;

    // List of online clients:
    private final HashMap<String, ClientInterface> onlineUsers = new HashMap<>();

    public ServerService(ServerController serverController) throws RemoteException {
        this.serverController = serverController;
    }


    /***************
     * RMI Methods *
     ***************/

    /**
     * RMI method to send message to clients,
     * used from the client to send message to the client
     **/
    public void sendToAll(String message) throws RemoteException {
        onlineUsers.forEach((username, client) -> {
            try {
                client.receiveMessage(message);
            } catch (RemoteException e) {
                System.out.println("Error sending message to " + username);
                // e.printStackTrace();
            }
        });
        serverController.printMessage(message.strip());
    }

    /**
     * RMI method to connect client to server,
     * used from the client to add client instance to the server,
     * update clients count and send the updated clients list to all clients
     **/
    public void addClient(String clientName, ClientInterface client) throws RemoteException {
        onlineUsers.put(clientName, client);
        serverController.printMessage("[+] " + clientName + " connected");

        // We can't update the UI from a non-JavaFX thread,
        // so we use Platform.runLater to run it on the JavaFX thread
        Platform.runLater(() -> serverController.updateClientsCount(onlineUsers.size()));
        sendClientsList();
    }

    /**
     * RMI method to disconnect client to server,
     * used from the client to remove client instance from the server
     * and send the updated clients list to all clients
     **/
    public void removeClient(String clientName) throws RemoteException {
        onlineUsers.remove(clientName);
        serverController.printMessage("[-] " + clientName + " disconnected");
        Platform.runLater(() -> serverController.updateClientsCount(onlineUsers.size()));
        sendClientsList();
    }


    /*******************
     * Non-RMI methods *
     *******************/

    // send clients list to all clients
    private void sendClientsList() {
        onlineUsers.forEach((username, client) -> {
            try {
                client.updateClientsList(new ArrayList<>(onlineUsers.keySet()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}
