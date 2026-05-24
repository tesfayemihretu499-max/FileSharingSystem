package filesharing.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FileService extends Remote {
    // User Authentication
    String registerUser(String username, String password, String email) throws RemoteException;
    String userLogin(String username, String password) throws RemoteException;

    // File Operations
    String uploadFile(String username, String fileName, byte[] fileData) throws RemoteException;
    List<String> getAllFiles() throws RemoteException;
    byte[] downloadFile(String fileName) throws RemoteException;

    // Messaging System
    String sendMessage(String sender, String receiver, String message) throws RemoteException;
    List<String> getMessages(String receiver) throws RemoteException;
}