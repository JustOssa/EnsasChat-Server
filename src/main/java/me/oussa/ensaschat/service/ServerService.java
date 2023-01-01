package me.oussa.ensaschat.service;

import javafx.application.Platform;
import me.oussa.ensaschat.common.ClientInterface;
import me.oussa.ensaschat.common.ServerInterface;
import me.oussa.ensaschat.controller.ServerController;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ServerService extends UnicastRemoteObject implements ServerInterface {

    private final ServerController serverController;

    // List of clients:
    private final ArrayList<ClientInterface> clients = new ArrayList<>();

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
        for (ClientInterface client : clients) {
            client.receiveMessage(message.strip());
        }
        serverController.printMessage(message.strip());
    }

    /**
     * RMI method to connect client to server,
     * used from the client to add client instance to the server,
     * update clients count and send the updated clients list to all clients
     **/
    public void addClient(ClientInterface client) throws RemoteException {
        clients.add(client);
        serverController.printMessage("[+] " + client.getClientName() + " connected");

        // We can't update the UI from a non-JavaFX thread,
        // so we use Platform.runLater to run it on the JavaFX thread
        Platform.runLater(() -> serverController.updateClientsCount(clients.size()));
        sendClientsList();
    }

    /**
     * RMI method to disconnect client to server,
     * used from the client to remove client instance from the server
     * and send the updated clients list to all clients
     **/
    public void removeClient(ClientInterface client) throws RemoteException {
        clients.remove(client);
        serverController.printMessage("[-] " + client.getClientName() + " disconnected");
        Platform.runLater(() -> serverController.updateClientsCount(clients.size()));
        sendClientsList();
    }


    /*******************
     * Non-RMI methods *
     *******************/

    // send clients list to all clients
    private void sendClientsList() throws RemoteException {
        for (ClientInterface client : clients) {
            client.updateClientsList(clients);
        }
    }
}
