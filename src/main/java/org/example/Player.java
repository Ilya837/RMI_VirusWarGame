package org.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import javafx.util.Pair;
public interface Player extends Remote {
    void Move(int i, int j, int playerNum) throws RemoteException;

    Pair<Integer,Integer> PlayerMove() throws RemoteException;

    void ConnectMe(int port, int playerNum) throws RemoteException;

    void PrintField() throws RemoteException;

    void Println(String str) throws RemoteException;
}
