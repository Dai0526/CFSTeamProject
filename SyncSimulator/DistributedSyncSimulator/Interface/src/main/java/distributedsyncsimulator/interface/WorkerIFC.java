package distributedsyncsimulator.ifc;

import java.rmi.*;

public interface WorkerIFC extends Remote {

    public void unlock() throws RemoteException;
    public void write() throws RemoteException;
}