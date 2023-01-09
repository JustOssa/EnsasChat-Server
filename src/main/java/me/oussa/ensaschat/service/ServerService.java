package me.oussa.ensaschat.service;

import javafx.application.Platform;
import me.oussa.ensaschat.common.ClientInterface;
import me.oussa.ensaschat.common.ServerInterface;
import me.oussa.ensaschat.controller.ServerController;
import me.oussa.ensaschat.model.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerService extends UnicastRemoteObject implements ServerInterface {

    private final ServerController serverController;

    private final HashMap<String, ClientInterface> onlineUsers = new HashMap<>();

    public ServerService(ServerController serverController) throws RemoteException {
        this.serverController = serverController;
    }


    /* *********** *
     * RMI Methods *
     * *********** */

    /**
     * RMI method to send message to clients,
     * used from the client to send message to the client
     *
     * @param message the message to send
     **/
    public void sendToAll(String message) throws RemoteException {
        onlineUsers.forEach((username, client) -> {
            try {
                client.receiveMessage(message);
            } catch (RemoteException e) {
                e.printStackTrace();
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
        sendOnlineClientsList();
    }

    /**
     * RMI method to disconnect client to server,
     * used from the client to remove client instance from the server,
     * update clients count and send the updated clients list to all clients
     **/
    public void removeClient(String clientName) throws RemoteException {
        onlineUsers.remove(clientName);
        serverController.printMessage("[-] " + clientName + " disconnected");
        Platform.runLater(() -> serverController.updateClientsCount(onlineUsers.size()));
        sendOnlineClientsList();
    }

    /**
     * RMI method to check username/password validity,
     * used from the client to sign in user
     *
     * @return the signed-in user
     **/
    @Override
    public User signIn(String username, String password) throws RemoteException {
        // already validated in client, but why not
        if (username.isBlank() || password.isBlank()) {
            return null;
        }

        // TODO: replace with database calls
        if (username.equals("admin") && password.equals("admin")) {
            return new User(username);
        }
        if (username.equals("user") && password.equals("user")) {
            return new User(username);
        }
        return null;
    }

    /**
     * RMI method to get all users from the db,
     *
     * @return list of all users
     **/
    // TODO: fetch users from db
    public List<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();
        users.add(new User("admin"));
        users.add(new User("user"));
        users.add(new User("test"));
        return users;
    }


    /* *************** *
     * Non-RMI methods *
     * *************** */

    // send online clients list to all clients
    private void sendOnlineClientsList() {
        onlineUsers.forEach((username, client) -> {
            try {
                client.updateOnlineUsers(new ArrayList<>(onlineUsers.keySet()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    // kick all clients
    public void kickAll() {
        onlineUsers.forEach((username, client) -> {
            try {
                client.getKicked();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        onlineUsers.clear();
        Platform.runLater(() -> serverController.updateClientsCount(onlineUsers.size()));
    }
}
