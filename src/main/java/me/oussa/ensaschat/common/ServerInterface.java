package me.oussa.ensaschat.common;

import me.oussa.ensaschat.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerInterface extends Remote {
    void sendToAll(String message) throws RemoteException;

    void addClient(String clientName, ClientInterface client) throws RemoteException;

    void removeClient(String clientName) throws RemoteException;

    User signIn(String username, String password) throws RemoteException;

    List<User> getUsers() throws RemoteException;
}
