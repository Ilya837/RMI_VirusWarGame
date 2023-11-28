package org.example;

import java.rmi.RemoteException;

public class Main {
    public static void main(String[] args) throws RemoteException {
        Game g = new Game();
        g.Start();
    }
}