package me.oussa.ensaschat.service;

import javafx.application.Platform;
import me.oussa.ensaschat.common.ClientInterface;
import me.oussa.ensaschat.common.ServerInterface;
import me.oussa.ensaschat.controller.ServerController;
import me.oussa.ensaschat.model.Message;
import me.oussa.ensaschat.model.User;
import me.oussa.ensaschat.model.UserDao;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
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
     * used from the client to send message to all clients
     *
     * @param message the message to send
     **/
    public void sendToAll(Message message) {
        onlineUsers.forEach((username, client) -> {
            try {
                client.receiveMessage(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        serverController.printMessage(message);
    }

    /**
     * RMI method to send message to a specific client,
     * used from the client to send message to a specific client
     *
     * @param message the message to send
     **/
    public void sendMessage(Message message) {
        try {
            ClientInterface receiver = onlineUsers.get(message.getReceiver().getUsername());
            receiver.receiveMessage(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * RMI method to connect client to server,
     * used from the client to add client instance to the server,
     * update clients count and send the updated clients list to all clients
     **/
    public void addClient(String clientName, ClientInterface client) throws RemoteException {
        // if already connected kick the old client
        if (onlineUsers.containsKey(clientName)) {
            onlineUsers.get(clientName).getKicked();
            serverController.printLog("[-] " + clientName + " disconnected");
        }

        onlineUsers.put(clientName, client);
        serverController.printLog("[+] " + clientName + " connected");

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
        serverController.printLog("[-] " + clientName + " disconnected");
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

        UserDao userDao = new UserDao();
        try {
            return userDao.signIn(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * RMI method Sign up user
     *
     * @param user the user to sign up
     * @return true if the user was signed up successfully, false otherwise
     */
    @Override
    public boolean signUp(User user) throws RemoteException {
        if (user.getName().isBlank() || user.getUsername().isBlank() || user.getPassword().isBlank()) {
            return false;
        }

        UserDao userDao = new UserDao();
        try {
            return userDao.signUp(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * RMI method to get all users from the db,
     *
     * @return list of all users
     **/
    public List<User> getUsers() {
        UserDao userDao = new UserDao();
        try {
            return userDao.getUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateUser(User user) throws RemoteException {
        UserDao userDao = new UserDao();
        try {
            return userDao.updateUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
