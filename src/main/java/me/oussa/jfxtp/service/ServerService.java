package me.oussa.jfxtp.service;

import me.oussa.jfxtp.common.ClientInterface;
import me.oussa.jfxtp.common.ServerInterface;
import me.oussa.jfxtp.controller.ServerController;

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


    /**
     * RMI method to send message to clients
     * used from the client to send message to the client
     **/
    public void sendToAll(String message) throws RemoteException {
        for (ClientInterface client : clients) {
            client.receiveMessage(message);
        }
        serverController.printMessage(message);
    }

    /**
     * RMI method to connect client to server
     * used from the client to add client instance to the server
     **/
    public void loginClient(ClientInterface client) throws RemoteException {
        clients.add(client);
        serverController.printMessage("[+] " + client.getClientName() + " connected");
    }

    /**
     * RMI method to disconnect client to server
     * used from the client to remove client instance from the server
     **/
    public void logoutClient(ClientInterface client) throws RemoteException {
        clients.remove(client);
        serverController.printMessage("[-] " + client.getClientName() + " disconnected");
    }
}
